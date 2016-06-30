#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <glib.h>
static char *key = "I tell a settlers tale of the old times";
static char *text;
/**
 * A simple and custom scrambling function for user data
 * @paramt text the text to scramble
 * @return a scrmabled version in base64 or NULL
 */
char *scramble( char *text )
{
    char *enciphered = malloc(strlen(text)+1);
    if ( enciphered != NULL )
    {
        gchar *res;
        int i;
        int klen = strlen(key);
        int tlen = strlen(text);
        for ( i=0;i<tlen;i++ )
            enciphered[i] = text[i] ^ key[i%klen];
        res = g_base64_encode(enciphered,strlen(text));
        free( enciphered );
        return (char*)res;
    }
    else
        return NULL;
}
static int check_args(int argc, char **argv )
{
    int sane = 1;
    if ( argc <= 4 )
    {
        int i;
        for ( i=1;i<argc-1;i++ )
        {
            if ( argv[i][0]=='-' && strlen(argv[i])==2 )
            {
                char opt = argv[i][1];
                switch ( opt )
                {
                    case 'k'://cipher key
                        if ( i < argc-2 )
                            key = argv[i+1];
                        else
                            sane = 0;
                        break;
                    default:
                        sane = 0;
                        break;
                }
            }
        }
        text = argv[argc-1];
    }
    else
       sane = 0;
    return sane;
}
int main( int argc, char **argv )
{
    if ( check_args(argc,argv) )
    {
        char *res = scramble( text );
        if ( res != NULL )
        {
            printf("%s",res);
            g_free(res);
            return 1;
        }
    }
    else
        printf("usage: encrypt [-k key] text\n");
   return 0;
}
