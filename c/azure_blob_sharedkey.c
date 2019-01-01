#include <openssl/hmac.h>
#include <openssl/evp.h>
#include <openssl/bio.h>
#include <openssl/buffer.h>
#include <string.h>
#include <stdio.h>

int HmacEncode(char * key, char * strToEncode, unsigned char ** output, unsigned int * output_length)
{
	EVP_MD * engine = EVP_sha256();
	*output = (unsigned char *)malloc(EVP_MAX_MD_SIZE);
	memset(*output, '\0', EVP_MAX_MD_SIZE);

    ENGINE_load_builtin_engines();
    ENGINE_register_all_complete();

	HMAC_CTX * ctx = HMAC_CTX_new();
	HMAC_Init_ex(ctx, key, strlen(key), EVP_sha256(), NULL);
	HMAC_Update(ctx, (unsigned char*)strToEncode, strlen(strToEncode));
	HMAC_Final(ctx, *output, output_length);
	HMAC_CTX_free(ctx);

	return 0;
}

char * Base64Encode(const char * input, int length)
{
	BIO * bmem = NULL;
	BIO * b64 = NULL;
	BUF_MEM * bptr = NULL;

	b64 = BIO_new(BIO_f_base64());
	//if(!with_new_line) {
		BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
	//}
	bmem = BIO_new(BIO_s_mem());
	b64 = BIO_push(b64, bmem);
	BIO_write(b64, input, length);
	BIO_flush(b64);
	BIO_get_mem_ptr(b64, &bptr);

	char * buff = (char *)malloc(bptr->length + 1);
	memcpy(buff, bptr->data, bptr->length);
	buff[bptr->length] = 0;

	BIO_free_all(b64);

	return buff;
}

char * Base64Decode(char * input, int length)
{
	BIO * b64 = NULL;
	BIO * bmem = NULL;
	char * buffer = (char *)malloc(length);
	memset(buffer, 0, length);

	b64 = BIO_new(BIO_f_base64());
	//if(!with_new_line) {
		BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
	//}
	bmem = BIO_new_mem_buf(input, length);
	bmem = BIO_push(b64, bmem);
	BIO_read(bmem, buffer, length);

	BIO_free_all(bmem);

	return buffer;
}

char* getAzureBlobSharedKey(char * storageKey, char * strToSign)
{
	//char* key = (char*)malloc(sizeof(char)*128);
	//base64_decode(storageKey, key);
	int storageKeyLen = strlen(storageKey);
	char* key = Base64Decode(storageKey, storageKeyLen);

	unsigned char * mac = NULL;
	unsigned int mac_length = 0;

	int ret = HmacEncode(key, strToSign, &mac, &mac_length);
	printf("mac_length=%d\n", mac_length);

	//char * sharedkey = (char*)malloc(256);
	//memset(sharedkey, '\0', 256);
	//base64_encode(mac, sharedkey);
	char* sharedkey = Base64Encode(mac, mac_length);

    free(key);
	if (mac) {
		free(mac);
	}

    return sharedkey;
}

int main(int argc, char * argv[])
{
    char * storageKey = NULL;
	char* strToSign = (char*)malloc(512);
	char gmtDate[] = "Thu, 22 Nov 2018 16:55:00 GMT";

    storageKey = "<your_storage_key>";
    sprintf(strToSign, "GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:%s\nx-ms-version:2015-02-21\n/storacctsuswest/images/01.jpg\0",gmtDate);

	//strToSign = "GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Thu, 22 Nov 2018 17:25:00 GMT\nx-ms-version:2015-02-21\n/storacctsuswest/images/01.jpg";

    char * sharedkey = getAzureBlobSharedKey(storageKey, strToSign);
   	printf("len = %d\n", strlen(sharedkey));
   	//printf("strToSign=%s\n", strToSign);
	printf("\nsharedkey=%s\n", sharedkey);
	printf("\ngmtDate=%s\n", gmtDate);

	//free(storageKey);
	free(strToSign);
    free(sharedkey);

	return 0;
}
