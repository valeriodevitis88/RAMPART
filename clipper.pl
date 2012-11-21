#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/modules";

# 3rd Part modules
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;

# Rampart modules
use QsOptions;
use SubmitJob;

# Setup directories
my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
my $NIZAR_SCRIPT_DIR = $RAMPART_DIR . "tools/nizar/";

# Tool constants
my $T_FASTX = "fastx";
my $T_NIZAR = "nizar";
my $DEF_TOOL = $T_NIZAR;

# Tool path constants
my $TP_FASTX = "fastx_clipper";
my $TP_NIZAR = $NIZAR_SCRIPT_DIR . "length_extract_fasta";
my $DEF_TOOL_PATH = $TP_NIZAR;

# Sourceing constants
my $SOURCE_FASTX 	= "source fastx_toolkit-0.0.13;";
my $FASTA_FORMATTER	= "fasta_formatter";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;

my $DEF_MIN_LEN = 1000;


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();


# Parse tool specific options
my (%opt) =  ( "min_length", $DEF_MIN_LEN );

GetOptions (
        \%opt,
        'min_length|minlen=i',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


my $cmd_line = "";


# Validation
die "Error: Minimum length not specified.\n\n" unless $opt{min_length};
die "Error: Input scaffolds file not specified.\n\n" unless $qst->getInput();
die "Error: Output scaffolds file not specified.\n\n" unless $qst->getOutput();

# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print	"\n\n" .
			$qst->toString() .
			"Minimum Length: " . $opt{min_length} . "\n\n";
}

my $output_file = $qst->getOutput() . "/clipped-scaffolds.fa";

# Select the scaffolder and build the command line
my $tool = $qst->getTool();
if ($tool eq $T_FASTX) {
	$cmd_line = $SOURCE_FASTX . " " . $TP_FASTX . " -l " . $opt{min_length} . " -i " . $qst->getInput() . " -o " . $output_file;
	die "Error: FastX clipper does not work correctly yet!\n\n";
}
elsif ($tool eq $T_NIZAR) {
	
	# Temporary file
	my $sl_file = $qst->getOutput() . "/in-sl.fa";
	
	# Convert input to single line seq format using FASTX
	my @sl_args = grep{$_} (
		$SOURCE_FASTX,
		$FASTA_FORMATTER,
		"-i " . $qst->getInput(),
		"-o " . $sl_file
	);
	my $sl_cmd = join " ", @sl_args;
	
	# Run Nizar's script
	my @nizar_args = grep{$_} (
		$TP_NIZAR,
		$opt{min_length},
		$sl_file,
		">",
		$output_file
	);	
	my $nizar_cmd = join " ", @nizar_args;
	
	# Combine the commands
	my @cmds = grep{$_} (
		$sl_cmd,
		$nizar_cmd
	);	
	my $cmd_line = join "; ", @cmds;
}
else {
	die "Error: Invalid tool requested.  Also, the script should not have got this far!!!.\n\n";
}


# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"Clipper has successfully submitted the clip job to the grid engine.  You will be notified by email when the clip job has completed.\n";
}

__END__

=pod

=head1 NAME

  clipper.pl


=head1 SYNOPSIS

  clipper.pl [options] --minlen <length_in_nt> -i <input_file> -o <output_file>

  For full documentation type: "clipper.pl --man"


=head1 DESCRIPTION

  This script is designed to clip reads from a fastq file that are shorter than a given minimum length, and to run this 
  process on a grid engine.  Currently there are two alternative methods for clipping: Nizar's own script and Fastx_clipper. 


=head1 OPTIONS

  --min_length			 --minlen
              The minimum length of a read in the output dataset.  All reads shorter than this are removed.  Default: 1000.

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported. Default: LSF.

  --tool                 -t
              Available clipping tools: (nizar, fastx). Default: nizar.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --memory               --mem               -m
              The amount of memory to reserve for this job.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --input                --in                -i
              REQUIRED: The input file for this job.

  --output               --out               -o
              REQUIRED: The output file for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

