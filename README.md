SEPTA-Android
=============

## Project Setup

### App Signing Set-up
1. In your user local home directory, create a file named <code>gradle.properties</code> in the <code>.gradle</code> directory.
This file is read by Android Studio upon startup

2. In this file, create one line, which will look like below:

	SEPTA.signing = /Users/<username>/.signing/SEPTASigning
__**__ _replace \<username> with your username_
		
3. Create a directory titled <code>.signing</code>.
4. Create a file titled <code>SEPTASigning</code>.

5. In the file titled <code>SEPTASigning</code>, create the lines, which will look like below:

		project.ext {

    	    SEPTA_Development_Keystore_File = '../../SEPTA-Keystores/septaandroid_development.jks'
        	SEPTA_Production_Keystore_File = '../../SEPTA-Keystores/septaandroid_production.jks'

   	 	    SEPTA_Development_KEYSTOREpassword = ''
    	    SEPTA_Production_KEYSTOREpassword = ''

      	  ALPHA_keyaliaspassword = ''
      	  PRERELEASE_keyaliaspassword = ''
      	  BETA_keyaliaspassword = ''
       	 RELEASE_keyaliaspassword = ''
		}

6. Create a directory titled <code>SEPTA-Keystores</code>, which will be located in the parent directory of this project.

7. Create two keystore files, one for development and one for production.
Once you have created the keystore files, add the password to the <code>SEPTASigning</code> configuration file.

8. Create the certificates in the keystore files as needed, add the password to the <code>SEPTASigning</code> configuration file.

#### Directories/Files
<dl>
<dt>Directory: <code>\<home directory>/.gradle</code></dt>
<dd>file: <code>gradle.properties</code></dd>
<dl>Directory: <code>\<home directory>/.signing</code></dt>
<dd>file: <code>SEPTASigning</code></dd>
</dl>

#### Content
<dl>
<dt>Directory: <code>\<home directory>/.gradle</code></dt>
<dd>file: <code>gradle.properties</code></dd>
<dd>	

			SEPTA.signing = /\<home directory>/.signing/SEPTASigning
</dd>
<dl>Directory: <code>\<home directory>/.signing</code></dt>
<dd>file: <code>SEPTASigning</code></dd>
<dd>

	project.ext {

 	    SEPTA_Development_Keystore_File = '../../SEPTA-Keystores/septaandroid_development.jks'
       	SEPTA_Production_Keystore_File = '../../SEPTA-Keystores/septaandroid_production.jks'

	    SEPTA_Development_KEYSTOREpassword = ''
   	    SEPTA_Production_KEYSTOREpassword = ''

   	    ALPHA_keyaliaspassword = ''
        PRERELEASE_keyaliaspassword = ''
        BETA_keyaliaspassword = ''
      	RELEASE_keyaliaspassword = ''
    }
</dd>
</dl>