#include <stdio.h>
#include <iostream.h>
#include <curl/curl.h>

using namespace std;

int download(string url, string local_file, int down_speed)
{
  CURL *image;
  CURLcode imgresult;
  FILE *fp;
  //url_download.c_str();

    image = curl_easy_init();
    if( image )
     {
        //Open File
        fp = fopen(local_file.c_str(), "w");
        if( fp == NULL ) cout << "File cannot be opened";
        
        curl_easy_setopt(image, CURLOPT_WRITEFUNCTION, NULL);
        curl_easy_setopt(image, CURLOPT_WRITEDATA, fp);
        curl_easy_setopt(image, CURLOPT_URL, url.c_str());
        curl_easy_setopt(image, CURLOPT_FOLLOWLOCATION, 1);
//这里限速 100KB/s
        curl_easy_setopt(image, CURLOPT_MAX_RECV_SPEED_LARGE, (curl_off_t)100 * 1024);
        curl_easy_setopt(image, CURLOPT_NOPROGRESS, 0);
        //CURLOPT_RESUME_FROM
        
        // Grab image
        imgresult = curl_easy_perform(image);
        if( imgresult )
            {
                cout << "Cannot grab the File!\n";
            }
    }
    //Clean up the resources
    curl_easy_cleanup(image);
    //Close the file
    fclose(fp);
    return 0;
}