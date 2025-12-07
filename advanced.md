# Advanced options

[Get started](./) | **Advanced**

## By photoset vs. collection

Flixport scans all photos in 2 different approaches, by photoset or by
collection. User makes the choice with `-w` or `--by_collection` and while
photoset is the default approach. By collection means Flixport traverses
all collections first, then iterates photoset in each leaf collection.
By photoset simply means it iterates all photosets. Why does it matter to user?

### Destination path

The way Flixport figures out the exact destination of a particular photo is
based on 3 options:

* `-d` or `-dest_spec`
* `-p` or `-dest_dir`
* `-n` or `-dest_file_name`

The destination of each photo is `<dest_spec><desc_dir>/<dest_file_name>`,
where dest_dir and dest_file_name supports $ syntax. In most cases the
default values of `dest_dir` and `dest_file_name` works well.

* The default value of dest_file_name is `${f.title}.${f.originalFormat}`.
* When photos are exported by photoset, the default value of dest_dir is
`/${s.title}`. 
* When photos are exported by collection, the default value of dest_dir
is `/${c.title}/${s.title}`.

With the default settings above command line like below

`java -jar flixport-0.0.1.jar -d s3:mybucket/flickr`

would copy photo `myphoto1.jpg` in Album `my_album` to s3 bucket `mybucket`
with key `flickr/my_album/myphoto1.jpg` when it exports photo by photoset.

If `my_album` is in collection `my_collection` and command line runs by
collection, the same photo would be
`flickr/my_collection/my_album/myphoto1.jpg`.

### More about the expression

In these expressions, `$f` is file, $s is a photoset and `$c` is a collection.

Only `$s` is available for `dest_dir` when photos are exported by photoset.
`$s` and $c are available for `dest_dir` when photos are exported by
collection.
`$f` and whatever is available for `dest_dir` are available for
`dest_file_format`.

## Execution options

### Max number of files
Max number of files can be limited by `-m` or `--max_files`, so that users
can get an idea what's going to happen in the full run. For example:

`java -jar flixport-0.0.1.jar -d s3:mybucket/flickr -m 20`

### Dry run mode

Another way to preview the execution is to dry run the command line tool
without actually copying any file. With `-r` or `--dry_run` option, Instead
of copying the file, the tool simply logs a message saying it would copy a
file from a location to a location. In dry run mode, it becomes particularly
important to keep the log files. For example:

`java -jar flixport-0.0.1.jar -d s3:mybucket/flickr -r 2>&1 | tee /tmp/flixport.log`

### Multi-thread

By default flixport runs in a single thread, which copies one file after
another. This is very inefficient if you have large number of files to copy.
Since the command line is making many TCP calls, the thread is mostly idle
while waiting for the calls to return. In your final run you almost always
need to specify number of threads to use with `-t` or `--threads` option to
keep the run time reasonable. For example:

`java -jar flixport-0.0.1.jar -d s3:mybucket/flickr -t 20`
