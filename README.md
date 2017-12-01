SEPTA-Android
=============

## License, Copyright and Trademark Disclaimer

Source code to the SEPTA Android application is available under the GPLv3 open source license, however, the SEPTA name, logo and graphics used in the app are not.  Those are subject to the following Copyright and Trademark terms:  http://www.septa.org/site/copyright.html

## Project Setup

### SQLite Database Preparation

1. ** Rename the id field of your table to “_id”. It can be easily done by clicking the Modify Table button, and then choosing the necessary table and field names. (not needed for this particular SQLite database file)

2. ** Add the android_metadata table. To do that, open the Execute SQL tab and paste this simple code into the SQL string field:  (no longer needed; was successful in loading the last SQLite file without this step)

    CREATE TABLE android_metadata (locale TEXT);

    INSERT INTO android_metadata VALUES ('en_US');
    
3. Compress the SQLite database file and put it into the "assets" directory for the app.

### App Signing Set-up
1. In your user local home directory, create a file named <code>gradle.properties</code> in the <code>.gradle</code> directory.
This file is read by Android Studio upon startup

2. In this file, create one line, which will look like below:

	SEPTA.signing = /Users/&lt;username&gt;/.signing/SEPTASigning
	android.enableAapt2 = false
		
3. Create a directory titled <code>.signing</code>.
4. Create a file titled <code>SEPTASigning.gradle</code>.

5. In the file titled <code>SEPTASigning.gradle</code>, create the lines, which will look like below:

		project.ext {
	        SEPTA_DEVELOPMENT_KEYSTORE_FILE = '../../SEPTA-Keystores/septaandroid_development.jks'
   	  		SEPTA_PRODUCTION_KEYSTORE_FILE  = '../../SEPTA-Keystores/septaandroid_production.jks'

	        SEPTA_DEVELOPMENT_KEYSTORE_PASSWORD = 'na'
	        SEPTA_PRODUCTION_KEYSTORE_PASSWORD  = 'na'

	        ALPHA_KEYALIAS_PASSWORD = 'na'
	        PRERELEASE_KEYALIAS_PASSWORD = 'na'
	        BETA_KEYALIAS_PASSWORD = 'na'
	        RELEASE_KEYALIAS_PASSWORD = 'na'
	        
            SPRINT_ROUND = '8'

	    	GOOGLE_GEO_API_KEY = 'na'
            AMAZONAWS_API_KEY = 'na'
    		CRASHLYTICS_API_KEY = 'na'

		}

6. Create a directory titled <code>SEPTA-Keystores</code>, which will be located in the parent directory of this project.

7. Create two keystore files, one for development and one for production.
Once you have created the keystore files, add the password to the <code>SEPTASigning</code> configuration file.

8. Create the certificates in the keystore files as needed, add the password to the <code>SEPTASigning</code> configuration file.

#### Directories/Files
<dl>
<dt>Directory: <code>&lt;home directory&gt;/.gradle</code></dt>
<dd>file:	<code>gradle.properties</code></dd>
<dt>Directory: <code>&lt;home directory&gt;/.signing</code></dt>
<dd>file:	<code>SEPTASigning</code></dd>
</dl>

#### Content
<dl>
<dt>Directory: <code>&lt;home directory&gt;/.gradle</code></dt>
<dd>file:	<code>gradle.properties</code></dd>
<dd>SEPTA.signing = /&lt;home directory&gt;/.signing/SEPTASigning</dd>
</dl>

<dl>Directory: <code>&lt;home directory&gt;/.signing</code></dt>
<dd>file:	<code>SEPTASigning.gradle</code></dd>
</dl>
		project.ext {
			SEPTA_DEVELOPMENT_KEYSTORE_FILE = '../../SEPTA-Keystores/septaandroid_development.jks'
			SEPTA_PRODUCTION_KEYSTORE_FILE  = '../../SEPTA-Keystores/septaandroid_production.jks'

			SEPTA_DEVELOPMENT_KEYSTORE_PASSWORD = 'na'
			SEPTA_PRODUCTION_KEYSTORE_PASSWORD  = 'na'

			ALPHA_KEYALIAS_PASSWORD = 'na'
			PRERELEASE_KEYALIAS_PASSWORD = 'na'
			BETA_KEYALIAS_PASSWORD = 'na'
			RELEASE_KEYALIAS_PASSWORD = 'na'

            SPRINT_ROUND = '8'

	    	GOOGLE_GEO_API_KEY = 'na'
            AMAZONAWS_API_KEY = 'na'
    		CRASHLYTICS_API_KEY = 'na'
		}
