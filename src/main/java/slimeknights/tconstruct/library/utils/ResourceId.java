package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Helper for use with our extensions of resource location for some type safety in IDs.
 * Note we left {@link ResourceLocation#withPath(String)} and alike as returning {@link ResourceLocation} as there is not much use extending an ID.
 * @see IdParser
 */
public abstract class ResourceId implements Comparable<ResourceId> {
  private final ResourceLocation location;

  protected static final class Dummy {}

  protected ResourceId(String namespace, String path, @Nullable Dummy pDummy) {
    this.location = ResourceLocation.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(ResourceLocation location) {
    this.location = location;
  }

  public ResourceId(String namespace, String path) {
    this.location = ResourceLocation.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(String location) {
    this.location = ResourceLocation.parse(location);
  }

  /** Gets the wrapped vanilla resource location. */
  public ResourceLocation location() {
    return location;
  }

  public String getNamespace() {
    return location.getNamespace();
  }

  public String getPath() {
    return location.getPath();
  }

  public ResourceLocation withPath(String path) {
    return location.withPath(path);
  }

  public ResourceLocation withSuffix(String suffix) {
    return location.withSuffix(suffix);
  }

  public ResourceLocation withPrefix(String prefix) {
    return location.withPrefix(prefix);
  }

  @Override
  public String toString() {
    return location.toString();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object instanceof ResourceId other) {
      return location.equals(other.location);
    }
    return location.equals(object);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location);
  }

  @Override
  public int compareTo(ResourceId other) {
    return location.compareTo(other.location);
  }

  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String,String,T> constructor) {
    String[] parts = IdParser.decompose(ResourceLocation.DEFAULT_NAMESPACE, string, ':');
    return tryBuild(parts[0], parts[1], constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (ResourceLocation.isValidNamespace(namespace) && ResourceLocation.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}
