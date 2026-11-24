# Login Google na versao da Play Store

## Problema

O login com Google funcionava nas versoes debug e release instaladas localmente, mas nao na versao distribuida pela Play Store. O SHA-1 da chave de assinatura usada pela Play Store nao estava entre os certificados Android do `google-services.json` usado para gerar o AAB.

## Como foi identificado e corrigido

1. Identificamos o caminho do APK instalado pela Play Store:

   ```powershell
   adb shell pm path com.cabral.lucrovarejo
   ```

2. Baixamos o `base.apk` do dispositivo usando o caminho retornado pelo comando:

   ```powershell
   adb pull "<caminho do base.apk>" ".\base-play.apk"
   ```

3. Consultamos o certificado de assinatura do APK:

   ```powershell
   & "<caminho do Android SDK>\build-tools\<versao>\apksigner.bat" verify --print-certs ".\base-play.apk"
   ```

   O APK instalado pela Play apresentou:

   - SHA-1: `4693a6db195dd21f5cbb4086ea14bee6dd0048a0`
   - SHA-256: `38724ce50a0e2b0bde1c116df4d1d6cd2fd38f592d9cd88cf07738335bccb688`

4. Cadastramos no Firebase os fingerprints da assinatura da Play Store, incluindo o SHA-1 obtido do APK instalado. Tambem cadastramos os fingerprints SHA-1 e SHA-256 de debug, release e Play Store.
5. Baixamos o `google-services.json` atualizado do Firebase e o colocamos em `app/google-services.json`.
6. Geramos e publicamos um novo AAB na Play Store. O login com Google passou a funcionar na versao instalada pela loja.

## Observacao

O `signingReport` do Gradle mostra os certificados locais de debug e release/upload; ele nao mostra a chave privada de assinatura do app mantida pela Play Store. Para diagnosticar uma versao distribuida pela loja, o certificado pode ser conferido no APK instalado com `apksigner`, ou consultado no Play Console em **Integridade do app > Assinatura do app**. A chave de assinatura do app da Play Store e diferente da chave de upload.
