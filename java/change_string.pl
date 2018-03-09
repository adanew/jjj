#!/usr/bin/env perl
use strict;
use warnings;

my @data_array;
open(my $fh, "<", "Physician.java")
    or die "Failed to open file: $!\n";
while(<$fh>) {
    chomp;
    push @data_array, $_;
}
close $fh;

#print join " ", @array;

open FILE, ">Physician_new.java";


foreach my $row (@data_array) {
    #print $loop_variable;
    my $string = $row;

    #Find line with pattern
    if ($string =~ m/textArea.append/) {
      #print "'$string' matches the pattern\n";

      #Substitute 1 line:
      #textArea.append(TimeStamp.getTimeStamp()+" - WAR: No Battery! \n");

      #for 2 lines:
      #textArea.append(TimeStamp.getTimeStamp()+" - WAR: No Battery! \n");
      #statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No Battery! \n");

      my @words = split (/textArea.append/, $string);

      #print $words[1]."\n";

      #foreach my $split_part (@words) {
      #  print $split_part;
      #}

      my $new_string = "statusLabel.setText".$words[1];
      #print $new_string."\n";

      print FILE $row."\n";
      print FILE $new_string."\n";

    }
    else{
      print FILE $row."\n";
    }
}
