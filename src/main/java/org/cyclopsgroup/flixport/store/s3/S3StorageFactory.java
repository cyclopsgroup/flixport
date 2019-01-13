package org.cyclopsgroup.flixport.store.s3;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.PrefixedDestinationStorage;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Preconditions;

public class S3StorageFactory extends DestinationStorageFactory {
  private static final Pattern S3_SPEC = Pattern.compile("^s3:((\\w|\\.|-)+)(/((\\w|\\.|-|/)*))?$");

  @Override
  public DestinationStorage createStorage(String storageSpec, String credentialSpec)
      throws IOException {
    Matcher m = S3_SPEC.matcher(storageSpec);
    Preconditions.checkArgument(m.matches() && m.groupCount() == 5,
        "Input %s isn't a valid S3 path", storageSpec);
    String prefix = m.group(4);

    // TODO: The way credentials are specified can be further improved here.
    AWSCredentialsProvider creds = new PropertiesFileCredentialsProvider(credentialSpec);

    return PrefixedDestinationStorage.decorate(new S3StorageClient(
        AmazonS3ClientBuilder.standard().withCredentials(creds).build(), m.group(1)), prefix);
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("s3:");
  }
}
