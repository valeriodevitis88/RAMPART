
.. _analyse_assemblies:

Analyse assemblies
==================

RAMPART currently offers 3 assembly analysis options in MASS:

* Contiguity
* Kmer read-assembly comparison
* Completeness

These can be identified using the following comma separated values in an attribute called ``types`` in the
``analyse_asms`` pipeline element.  The available options for the list are: QUAST,KMER,CEGMA.

QUAST, compares the assemblies from a contuiguity perspective.  This tool runs really fast, and produces statistics such
as the N50, assembly size, max sequence length.  It also produces a nice html report showing cumulative length
distribution curves for each assembly and GC content curves.

KMER, performs a kmer count on the assembly using Jellyfish, and, assuming kmer counting was requested on the reads
previously, will use the Kmer Analysis Toolkit (KAT) to create a comparison matrix comparing kmer counts in the reads to
the assembly.  This can be visualised later using KAT to show how much of the content in the reads has been assembled
and how repetitive the assembly is.  Repetition could be due to heterozygosity in the diploid genomes so please read the
KAT manual and walkthrough guide to get a better understanding of how to interpret this data.

CEGMA aligns highly conserved eukaryotic genes to the assembly.  CEGMA produces a statistic which represents an estimate
of gene completeness in the assembly.  i.e. if we see CEGMA maps 95% of the conserved genes to the assembly we can
assume that the assembly is very approximately 95% complete.  This is a very rough guide and shouldn't be taken
literally, but can be useful when comparing other assemblies made from the same data.  CEGMA has a couple of other
disadvantages however, first it is quite slow, second it only works on eukayortic organisms so is useless for bacteria.


Selecting the best assembly
---------------------------

Assuming at least one analysis option is selected, RAMPART will produce a table listing each assembly as a row, with each
column representing an assembly metric.  The user can specify a weighting file when running RAMPART to assign the
weights to each metric.  Each assembly is then assigned a score, based on the weighted mean of the metrics, and the
assembly with the highest score is then automatically selected as the **best** assembly to be used downstream.

Should the user wish to override the default weights that are assigned to each assembly metric, they can do so by
setting the ``weightings_file`` attribute in the ``mass`` element.  For example, using an absolute path to a custom
weightings file the XML snippet may look like this::

   <analyse_asms types="QUAST,KMER,CEGMA" parallel="false" threads="16"
                 weightings_file="~/.tgac/rampart/custom_weightings.tab"/>

The format of the weightings file is a pipe separated table as follows::

   nb_seqs|nb_seqs_gt_1k|nb_bases|nb_bases_gt_1k|max_len|n50|l50|gc%|n%|completeness
   0.05|0.1|0.05|0.05|0.05|0.2|0.05|0.05|0.1|0.3

All the metrics are derived from Quast results, except for the last one.

TODO: Currently the kmer metric, is not included.  In the future this will offer an alternate means of assessing the
assembly completeness.