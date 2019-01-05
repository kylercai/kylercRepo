#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "urldecode.h"
#include "urlencode.h"

char * getValidURL(char * s_str, char* validURL) {
  char * protPrefix = (char*)malloc(16);
  char * urlToEncode = (char*)malloc(BURSIZE);

  char *de_str = urldecode(s_str);
  printf("de_str = %s\n", de_str);

  int protPrefixPos = substrpos(de_str, "://");

  memset(urlToEncode, '\0', BURSIZE);
  memset(protPrefix, '\0', 16);
  if ( protPrefixPos != -1 ) {
    strncpy(protPrefix, de_str, protPrefixPos+3);
    strncpy(urlToEncode, de_str+protPrefixPos+3, strlen(de_str)-(protPrefixPos+3) );
  } else {
    strcpy(urlToEncode, de_str);
  }

  char *en_str = urlencode(urlToEncode);
  if ( strcmp("", protPrefix) != 0 ) {
        sprintf(validURL, "%s%s", protPrefix, en_str);
  } else {
      strcpy(validURL, en_str);
  }

  free(protPrefix);
  free(urlToEncode);
  free(de_str);
  
  return validURL;
}

int main(void) {
  char *s_str = NULL;
  char *validURL = (char*)malloc(BURSIZE);

  s_str = "/export/home0/docs/news/special/C12564/20040714/images/100566__��___��_______��_.swf";
  printf("in_URL = %s\n", s_str);
  getValidURL(s_str, validURL);
  printf("validURL = %s\n\n", validURL);

  s_str = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/100566__¨___ì_______¨_.jpg";
  printf("in_URL = %s\n", s_str);
  getValidURL(s_str, validURL);
  printf("validURL = %s\n\n", validURL);

  s_str = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/101103____________________¤____.jpg";
  printf("in_URL = %s\n", s_str);
  getValidURL(s_str, validURL);
  printf("validURL = %s\n\n", validURL);

  s_str = "https://storacctsuswest.blob.core.windows.net/export/home0/docs/life/ttys/p/~`!@#$%25^&(),.[]{}+_- 2345678.jpg";
  printf("in_URL = %s\n", s_str);
  getValidURL(s_str, validURL);
  printf("validURL = %s\n\n", validURL);

  //s_str = "¨";
  //s_str = ".jpg";
  //s_str = "+_- 2345678.jpg";


  free(validURL);
  return 0;
}
