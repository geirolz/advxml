## Gen Key Pair
gpg --gen-key

## Export public key
gpg --armor --export %LONG_ID% > advxml-bot2-public.gpg

## Export private key in base64
gpg --armor --export-secret-keys %LONG_ID% | openssl base64  > advxml-bot2-private.gpg
    