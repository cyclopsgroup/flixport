package org.cyclopsgroup.flixport.store.s3;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.DestinationStorageOptions;
import org.cyclopsgroup.flixport.store.PrefixedDestinationStorage;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;

public class S3StorageFactory extends DestinationStorageFactory {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final Pattern S3_SPEC = Pattern.compile("^s3:((\\w|\\.|-)+)(/((\\w|\\.|-|/)*))?$");

  @Override
  public DestinationStorage createStorage(DestinationStorageOptions options) throws IOException {
    Matcher m = S3_SPEC.matcher(options.getDestSpec());
    Preconditions.checkArgument(m.matches() && m.groupCount() == 5,
        "Input %s isn't a valid S3 path", options.getDestSpec());
    String prefix = m.group(4);

    AWSCredentialsProvider creds;
    if (!Strings.isNullOrEmpty(options.getDestCredentialsFile())) {
      logger.atInfo().log("Reading AWS credentials from file %s.",
          options.getDestCredentialsFile());
      creds = new PropertiesFileCredentialsProvider(options.getDestCredentialsFile());
    } else if (!Strings.isNullOrEmpty(options.getAwsKey())) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(options.getAwsSecret()),
          "AWS secret is not specified while the key is.");
      logger.atInfo().log("Using AWS key and secret specified from command line options.");
      creds = new AWSStaticCredentialsProvider(
          new BasicAWSCredentials(options.getAwsKey(), options.getAwsSecret()));
    } else {
      throw new IllegalStateException(
          "Please specify AWS credentials with a file or command line options.");
    }
    return PrefixedDestinationStorage.decorate(new S3StorageClient(
        AmazonS3ClientBuilder.standard().withCredentials(creds).build(), m.group(1)), prefix);
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("s3:");
  }
}
