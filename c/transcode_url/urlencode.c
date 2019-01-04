#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include "urlencode.h"

// Function to replace a string with another
// string
char *replaceWord(const char *s, const char *oldW,
                                 const char *newW)
{
    char *result;
    int i, cnt = 0;
    int newWlen = strlen(newW);
    int oldWlen = strlen(oldW);

    // Counting the number of times old word
    // occur in the string
    for (i = 0; s[i] != '\0'; i++)
    {
        if (strstr(&s[i], oldW) == &s[i])
        {
            cnt++;

            // Jumping to index after the old word.
            i += oldWlen - 1;
        }
    }

    // Making new string of enough length
    result = (char *)malloc(i + cnt * (newWlen - oldWlen) + 1);
    char de_str[i + cnt * (newWlen - oldWlen) + 1];

    i = 0;
    while (*s)
    {
        // compare the substring with the result
        if (strstr(s, oldW) == s)
        {
            strcpy(&result[i], newW);
            i += newWlen;
            s += oldWlen;
        }
        else
            result[i++] = *s++;
    }

    result[i] = '\0';
    strcpy(de_str, result);
    free(result);
    result = NULL;
    return de_str;
}

char *strrpc(char *str,char *oldstr,char *newstr){
    char bstr[strlen(str)];//转换缓冲区
    memset(bstr,0,sizeof(bstr));

    for(int i = 0;i < strlen(str);i++){
        if(!strncmp(str+i,oldstr,strlen(oldstr))){//查找目标字符串
            strcat(bstr,newstr);
            i += strlen(oldstr) - 1;
        }else{
        	strncat(bstr,str + i,1);//保存一字节进缓冲区
	    }
    }

    strcpy(str,bstr);
    return str;
}

int substrpos(char *str,char *substr){
    int pos = -1;

    for(int i = 0;i < strlen(str);i++){
        if(strncmp(str+i,substr,strlen(substr)) == 0){//查找目标字符串
            pos = i;
            break;
        }
    }

    return pos;
}

char dec2hex(short int c)
{
    if (0 <= c && c <= 9)
    {
        return c + '0';
    }
    else if (10 <= c && c <= 15)
    {
        return c + 'A' - 10;
    }
    else
    {
        return -1;
    }
}

void encode(char url[])
{
    int i = 0;
    int len = strlen(url);
    int res_len = 0;
    char res[BURSIZE];
    for (i = 0; i < len; ++i)
    {
        char c = url[i];
        if (    ('0' <= c && c <= '9') ||
                ('a' <= c && c <= 'z') ||
                ('A' <= c && c <= 'Z') ||
                c == '/' || c == '.' ||
                c == '('|| c == ')' ||
                c == '!' || c == '*' ||
                c == '~' || c == '_' ||
                c == '-' || c == '\'')
        {
            res[res_len++] = c;
        }
        else
        {
            int j = (short int)c;
            if (j < 0)
                j += 256;
            int i1, i0;
            i1 = j / 16;
            i0 = j - i1 * 16;
            res[res_len++] = '%';
            res[res_len++] = dec2hex(i1);
            res[res_len++] = dec2hex(i0);
        }
    }
    res[res_len] = '\0';
    strcpy(url, res);
}

// {"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08",
// "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F", "%10", "%11", "%12", "%13", "%14",
// "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%7F"};
// 对上面urlencode后再次进行urlencode编码
char* urlencode(char *url){
  encode(url);
  char *control_chars_code[] = {"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08",
            "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F", "%10", "%11", "%12", "%13", "%14",
            "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%7F"};

  char *result = NULL;
  int i;
  for (i = 0; i < sizeof(control_chars_code)/sizeof(char *); i++) {

    char temp[10];
    strcpy(temp, control_chars_code[i]);

    if (strstr(url, temp) != NULL) {
      urlencode(temp);
      //result = replaceWord(url, control_chars_code[i], temp);
      result = strrpc(url, control_chars_code[i], temp);
      url = result;
    }
  }

  return url;
}
