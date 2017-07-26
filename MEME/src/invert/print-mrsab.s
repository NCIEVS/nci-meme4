#!/bin/csh -f

#print out MRSAB in a readable way:

awk -F\| '{print("VCUI|"$1"|\n")}' $1
awk -F\| '{print("RCUI|"$2"|\n")}' $1
awk -F\| '{print("VSAB|"$3"|\n")}' $1
awk -F\| '{print("RSAB|"$4"|\n")}' $1
awk -F\| '{print("SON|"$5"|\n")}' $1
awk -F\| '{print("SF|"$6"|\n")}' $1
awk -F\| '{print("SVER|"$7"|\n")}' $1
awk -F\| '{print("VSTART|"$8"|\n")}' $1
awk -F\| '{print("VEND|"$9"|\n")}' $1
awk -F\| '{print("IMETA|"$10"|\n")}' $1
awk -F\| '{print("RMETA|"$11"|\n")}' $1
awk -F\| '{print("SLC|"$12"|\n")}' $1
awk -F\| '{print("SCC|"$13"|\n")}' $1
awk -F\| '{print("SRL|"$14"|\n")}' $1
awk -F\| '{print("TFR|"$15"|\n")}' $1
awk -F\| '{print("CFR|"$16"|\n")}' $1
awk -F\| '{print("CXTY|"$17"|\n")}' $1
awk -F\| '{print("TTYL|"$18"|\n")}' $1
awk -F\| '{print("ATNL|"$19"|\n")}' $1
awk -F\| '{print("LAT|"$20"|\n")}' $1
awk -F\| '{print("CENC|"$21"|\n")}' $1
awk -F\| '{print("CURVER|"$22"|\n")}' $1
awk -F\| '{print("SABIN|"$23"|\n")}' $1
awk -F\| '{print("SSN|"$24"|\n")}' $1
awk -F\| '{print("SCIT|"$25"|\n")}' $1

