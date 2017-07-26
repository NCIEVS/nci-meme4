#include <stdio.h>
#define	ERROUT	2
char *cchar[256] = {
	"NUL" , "SOH" , "STX" , "ETX" , "EOT" , "ENQ" , "ACK" , "BEL" ,
	"BS"  , "HT"  , "LF"  , "VT"  , "FF"  , "CR"  , "SO"  , "SI"  ,
	"DLE" , "DC1" , "DC2" , "DC3" , "DC4" , "NAK" , "SYN" , "ETB" ,
	"CAN" , "EM"  , "SUB" , "ESC" , "FS"  , "GS"  , "RS"  , "US"  ,
	"SPACE"," "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , " "   ,
	" "   , " "   , " "   , " "   , " "   , " "   , " "   , "DEL" ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   " ,
	"   " , "   " , "   " , "   " , "   " , "   " , "   " , "   "};

main (argc, argv)
int argc;
char *argv[];
{
	long ccount[256], total[5], totch;
	int i, j, k, fp;
	unsigned char buf[1024];

	for (i = 0; i < 256; i++) {
		ccount[i] = 0L;
		if (i > 32 && i < 127) {
                        char *new = (char *)malloc(4);
			sprintf(new, "%c", (char)i);
			cchar[i] = new;
			}
		else if (i > 127) {
                        char *new = (char *)malloc(4);
			sprintf(new, "%3d", i);
			cchar[i] = new;
			}
		}
	totch = 0L;
	for (i = 0; i < 5; total[i++] = 0L);
	if (argc <= 1) {
		sprintf(buf,"Usage: chc file [ file ... ]\n");
		write(ERROUT, buf, strlen(buf));
		exit();
		}

	for (k = 1; k < argc; k++) {
		if ((fp = open(argv[k], 0)) < 0) {
			sprintf(buf,"Unable to open %s for reading\n",argv[k]);
			write(ERROUT, buf, strlen(buf));
			}
		else while ((i = read(fp, buf, 1024)) > 0)
			for (j = 0; j < i; j++) {
				ccount[buf[j]]++;
				totch++;
				}
		close(fp);
		}
	if (totch) {
		printf("\n Code Char  Number   Code Char  Number");
		printf("   Code Char  Number   Code Char  Number\n\n");
		for (i = 0; i < 32; i++) {
			printf(" %3d %5s %7ld  ",i,cchar[i],ccount[i]);
			total[0] += ccount[i];
			printf(" %3d %5s %7ld  ",i+32,cchar[i+32],ccount[i+32]);
			if (i > 15 && i < 26)
				k = 1;
			else
				k = 3;
			total[k] += ccount[i+32];
			printf(" %3d %5s %7ld  ",i+64,cchar[i+64],ccount[i+64]);
			printf(" %3d %5s %7ld\n",i+96,cchar[i+96],ccount[i+96]);
			if (i > 0 && i < 27)
				k = 2;
			else
				k = 3;
			total[k] += ccount[i+64] + ccount[i+96];
			}
		printf("\nEBCDIC characters\n");
		for (i = 0; i < 32; i++) {
			printf(" %3s %5X %7ld  ",cchar[i+128],i+128,ccount[i+128]);
			printf(" %3s %5X %7ld  ",cchar[i+160],i+160,ccount[i+160]);
			printf(" %3s %5X %7ld  ",cchar[i+192],i+192,ccount[i+192]);
			printf(" %3s %5X %7ld\n",cchar[i+224],i+224,ccount[i+224]);
			total[4] += ccount[i+128] + ccount[i+160] + ccount[i+192] + ccount[i+224];
			}
		printf("\nTotal control characters - %16ld\n",total[0]);
		printf("Total numerical characters - %14ld\n",total[1]);
		printf("Total alphabetic characters - %13ld\n",total[2]);
		printf("Total occurrences of spaces - %13ld\n",ccount[32]);
		total[3] -= ccount[32];
		printf("Total other graphic characters - %10ld\n",total[3]);
		printf("Total EBCDIC characters - %17ld\n",total[4]);
		printf("\n     Total characters read - %14ld\n",totch);
		}
}
