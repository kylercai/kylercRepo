#define BURSIZE 2048

void encode(char url[]);

char* urlencode(char *url);

char dec2hex(short int c);

int substrpos(char *str,char *substr);

char *strrpc(char *str,char *oldstr,char *newstr);
