#!/usr/bin/perl

use strict;

#### Packages
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsOptions;
use Configuration;
use SubmitJob;

#### Constants

# Project constants
my $JOB_PREFIX = $ENV{'USER'} . "-rampart-";


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Assembly stats gathering constants
my $MASS_PATH = $RAMPART_DIR . "mass.pl";
my $MASS_SELECTOR_PATH = $RAMPART_DIR . "mass_selector.pl";
my $IMPROVER_PATH = $RAMPART_DIR . "improver.pl";

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Gather Command Line options and set defaults
my (%opt) = (	"mass", 			1,
				"improver",			1,
				"mass_selector", 	1 );
	

GetOptions (
	\%opt,
	'mass!',
	'mass_args|ma=s',
	'mass_selector!',
	'improver!',
	'improver_args|ia=s',
	'raw_config|rc=s',
	'qt_config|qtc=s',
	'simulate|sim',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No raw library config file specified\n\n" unless $opt{raw_config};
die "Error: No quality trimmed library config file specified\n\n" unless $opt{qt_config};
#die "Error: Approximate genome size not specified\n\n" unless $opt{approx_genome_size};


# Interpret config files
my $raw_cfg = new Configuration( $opt{raw_config} );
my $qt_cfg = new Configuration( $opt{qt_config} );



#### Process (all steps to be controlled via cmd line options)
my $mass_job_prefix = $qst->getJobName() . "-mass";
my $ms_job_name = $qst->getJobName() . "-ms";
my $get_best_job_name = $mass_job_prefix . "-getbest";
my $improver_job_prefix = $qst->getJobName() . "-improver";


#Set locations of important assembly directories and files
my $mass_dir = $qst->getOutput() . "/mass";
my $raw_mass_dir = $mass_dir . "/raw";
my $qt_mass_dir = $mass_dir . "/qt";
my $raw_stats_file = $raw_mass_dir . "/stats.txt";
my $qt_stats_file = $qt_mass_dir . "/stats.txt";
my $best_path_file = $mass_dir . "/best.path.txt";
my $best_dataset_file = $mass_dir . "/best.dataset.txt";


## Run assemblies for both raw and qt datasets
if ($opt{mass}) {

	mkdir $mass_dir;	
	mkdir $raw_mass_dir;	
	mkdir $qt_mass_dir;

	my $raw_input = join " ", $raw_cfg->getAllInputFiles();
	my $qt_input = join " ", $qt_cfg->getAllInputFiles();
	my $raw_mass_job_prefix = $mass_job_prefix . "-raw";
	my $qt_mass_job_prefix = $mass_job_prefix . "-qt";

	# Run the assembler script for each dataset
	run_mass($raw_input, $raw_mass_job_prefix, $raw_mass_dir);
	run_mass($qt_input, $qt_mass_job_prefix, $qt_mass_dir);
}

if ($opt{mass_selector}) {
	
	my @ms_args = grep {$_} (
			$MASS_SELECTOR_PATH,
			$qst->getGridEngineAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $ms_job_name,
			$opt{mass} ? "--wait_condition 'ended(" . $mass_job_prefix . "*)'" : "",
			$qst->getQueueAsParam(),
			$qst->getExtraArgs(),
			"--output " . $mass_dir,
			$qst->isVerboseAsParam(),
			"--raw_stats_file " . $raw_stats_file,
			"--qt_stats_file " . $qt_stats_file,
			$opt{approx_genome_size} ? "--approx_genome_size " . $opt{approx_genome_size} : "" );
			
	my $ms_cmd_line = join " ", @ms_args;

	system($ms_cmd_line);
}

## Improve best assembly

if ($opt{improver}) {

	# First find the best assembly
	my ($best_file, $best_dataset) = getBest($best_path_file, $best_dataset_file, $ms_job_name, $get_best_job_name);
	my $best_config = (($best_dataset eq "raw") ? $opt{raw_config} : $opt{qt_config});
	my $best_config_data = (($best_dataset eq "raw") ? $raw_cfg : $qt_cfg);
	
	if ($qst->isVerbose()) {
		print 	"\n" .
				"Best assembly is: " . $best_file . "\n" .
				"Best dataset is: " . $best_dataset . "\n" . 
				"Best config file is: " . $best_config . "\n\n";
	}

	# Then run improver.
	my $imp_dir = $qst->getOutput() . "/improver";
	mkdir $imp_dir;
	
	chdir $imp_dir;

	my @imp_args = grep {$_} (
			$IMPROVER_PATH,
			$qst->getGridEngineAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $improver_job_prefix,
			"--wait_condition 'done(" . $get_best_job_name . ")'",
			$qst->getQueueAsParam(),
			$qst->getExtraArgs(),
			"--output " . $imp_dir,
			"--input " . $best_file,
			"--config " . $best_config,
			"--stats",
			"--degap_args \"--read_length " . $best_config_data->getSectionAt(0)->{max_rd_len} . "\"",
			$opt{simulate} ? "--simulate" : "",
			$opt{improver_args},
			$qst->isVerboseAsParam());

	system(join " ", @imp_args);
	
	chdir $PWD;
}

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"RAMPART has successfully submitted all child jobs to the grid engine.  You will be notified by email when the jobs have completed.\n";
}


sub run_mass {

	my @mass_args = grep {$_} (	
		$MASS_PATH,
		$qst->getGridEngineAsParam(),
		$qst->getProjectNameAsParam(),
		"--job_name " . $_[1],
		$qst->getQueueAsParam(),
		$qst->getExtraArgs(),
		"--output " . $_[2],
		$qst->isVerboseAsParam(),
		$opt{mass_args},
		"--stats",
		$opt{simulate} ? "--simulate" : "",
		$_[0] 
	);

	system(join " ", @mass_args);
}

sub getBest {
	my $best_path_file = shift;
	my $best_dataset_file = shift;
	my $wait_job = shift;
	my $job_name = shift;


	# Wait for mass selector to complete 
	my $best_wait = new QsOptions();
	$best_wait->setGridEngine($qst->getGridEngine());
	$best_wait->setProjectName($qst->getProjectName());
	$best_wait->setJobName($job_name);
	$best_wait->setWaitCondition("ended(" . $wait_job . ")") if $opt{mass_selector};
	$best_wait->setExtraArgs("-I");	# This forces this job to stay connected to the terminal until the wait job has ended
	SubmitJob::submit($best_wait, "sleep 1");

	if ($qst->isVerbose()) {
		print 	"\n" . 
				"Attempting to load best assembly file from: " . $best_path_file . "\n";
	}

	# Now the the files exist read them and return the values they contain

	open BP, "<", $best_path_file or die "Error: Couldn't parse input file.\n\n";
	my @bplines = <BP>;
	die "Error: Was only expecting a single line.\n\n" unless (@bplines == 1);
	my $best_path = $bplines[0];
	close(BP);

	open BD, "<", $best_dataset_file or die "Error: Couldn't parse input file.\n\n";
	my @bdlines = <BD>;
	die "Error: Was only expecting a single line.\n\n" unless (@bdlines == 1);
	my $best_dataset = $bdlines[0];
	close(BD);
	
	$best_path =~ s/\s+$//;
	$best_dataset =~ s/\s+$//;

	return ($best_path, $best_dataset);
}



__END__

=pod

=head1 NAME

  rampart.pl


=head1 SYNOPSIS

  rampart.pl [options] --raw_config <raw_config_file> --qt_config <qt_config_file>

  For full documentation type: "rampart.pl --man"


=head1 DESCRIPTION

  This script is designed to run mass on raw and quality trimmed datasets and gather the resulting statistics.  
  It then selects the best assembly and then attempts to improve that assembly by doing additional scaffolding, gap closing and cleaning.


=head1 OPTIONS

  --mass
	          Whether or not to do the MASS step.  Use --nomass to disable.  Default: on.
	          
  --mass_args            --ma
              Any additional arguments to pass to the MASS tool (e.g. --kmin and --kmax).
	
  --mass_selector
              Whether to attempt to select the best assembly from a set of assemblies already created by rampart.  Use --nomass_selector to disable.  Default: on.
	
  --improver
              Whether or not to run the assembly improver step.  Use --noimprover to disable.  Default: on.
	
  --improver_args        --ia
              Any additional arguments to pass to the improver tool (e.g. --iterations)
	
  --raw_config           --rc
              REQUIRED: The path to the rampart library configuration file for the raw dataset.
	
  --qt_config            --qtc
              REQUIRED: The path to the rampart library configuration file for the quality trimmed dataset.
	
  --simulate             --sim
              If set then the script is run but no mass or improver jobs are submitted to the grid engine. Default: off.
	
  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut



