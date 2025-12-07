# Get started with Flixport

**Get started** | [Advanced](advanced.md)

## Prerequisites
Can wait to try it? Good. Before start, make sure to go through the checklist
below. These steps only need to be done once.

### Command line environment
As of now Flixport is a command line tool. To use it, you should feel
comfortable with command line environment. This means different setups in
Windows, Mac or Linux.

### Install Java
If you haven't, download and install Java. Configure it properly so that it's
available in command line environment.

Java version 8 or above is required. Verify your Java installation with the
command below.

```
me@me:~/code/flixport$ java -version
openjdk version "1.8.0_171-google-v7"
OpenJDK Runtime Environment (build 1.8.0_171-google-v7-228093407-228093407)
OpenJDK 64-Bit Server VM (build 25.171-b01, mixed mode)
```

### Download Flixport
Find the latest release of Flixport in Github release page. Download the
`flixport-#.#.#.jar` file. Go to the directly with the download jar file, you
can verify the jar file by running it with `-h` for help.

```
me@me:~/Downloads$ java -jar flixport-0.0.1.jar -h
[USAGE]
  flixport <OPTIONS>
[OPTIONS]
  -n --aws_region     <value>   AWS region
  -k --app_key        <value>   Flickr application key
...
```

### Create an API key in Flickr

Goto https://www.flickr.com/services/api, click "API keys" and create a key
if you haven't done so.

Find details in http://docs.cyclopsgroup.org/flixport

## Configure Flixport

Flixport command line is mostly configured in `$HOME/.flixport/cli.properties`
file. The properties file can define any command line options with long name
as the property name. In another word if you set an option with the long name
in cli.properties file, it becomes default so you don't need to specify in
command line. Therefore typically you would configure:

### Flickr app

Flick API key and secret

```
app_key=0123456789abcdef0123456789abcdef
app_secret=0123456789abcdef
```

### S3 IAM

If you are exporting photos to S3 , you can store AWS IAM key and secret in
properties file.

```
aws_key=AKIA0123456789ABCDEF
aws_secret=0123456789ABCDEF0123456789ABCDEF01234567
```

## Run it

### Quick examples

```
java -jar flixport-0.0.1.jar -d file:$HOME/Downloads/flickr -t 20
java -jar flixport-0.0.1.jar -d s3:mys3bucket/some/path -t 20
```

`-t 20` runs the command in 20 threads in parallel.

### Options

Before start, take a look at the available command line options. Run it with
`-h` or `--help` to see them.

```
me@me:~/Downloads$ java -jar flixport-0.0.1.jar -h
[USAGE]
  flixport <OPTIONS>
[OPTIONS]
  -n --aws_region     <value>   AWS region
  -k --app_key        <value>   Flickr application key
  -r --dry_run                  Log action without actually copying files
  -t --threads        <integer> Number of threads to use
  -y --aws_key        <value>   AWS credentials key
  -z --aws_secret     <value>   AWS credentials secret
  -x --flixport_dir   <dir>     Directory for application specific local files
  -d --dest_spec      <value>   Destination spec
  -e --max_attempts   <integer> Max number of attempts to export a photoset.
  -w --by_collection            Nested output by collection
  -m --max_files      <integer> Max number of files to copy
  -n --dest_file_name <format>  File name of the output file
  -h --help                     Show help message
  -c --dest_creds     <file>    Destination storage credentials file or spec
  -p --dest_dir       <dir>     Directory of destination
  -a --force_auth               Force to authenticate
  -s --app_secret     <value>   Flickr application secret
```

The destination, `-d` or `--dest_spec` is a mandatory option that specifies
where to export photos to. As of now it supports:

* S3: `-d s3:mys3bucket/some/optional/path`
* Google storage service: `-d gs:mygcsbucket/some/optional/path`
* Local disk: `-d file:/tmp/my/flickr`

In case of S3, IAM key and secret is required. They are specified with
`--aws_key` and `--aws_secret` options in command line or `cli.properties`.

In case of Google storage service, JSON key file is required. You can specify
it with `-c` or `--dest_creds` with the path to the key file. For example:

`java -jar flixport-0.0.1.jar -d gs:mygcsbucket/some/optional/path -c $HOME/my-gcp-key.json`

To export to local disk, no other options are required.

`java -jar flixport-0.0.1.jar -d file:$HOME/Downloads/flickr`

### Preview with a limit

Before exporting all photos, you almost always want to run it with a limit for
the number of photos to export. This step gives you and idea how the result
will look like and where the files end up. File limit is set with `-m` or
`--max_files` options.

You can run the command over and over again, it exports the given number of
new photos a time incrementally.

