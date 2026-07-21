package slimeknights.tconstruct.test;

import com.google.gson.JsonElement;
import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.data.listener.MergingJsonDataLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Extension of {@link JsonFileLoader} with extra functionality to mock multiple data packs
 * @param <B>  Builder type
 */
public class MergingJsonFileLoader<B> extends JsonFileLoader {
  private final MergingJsonDataLoader<B> dataLoader;
  private final Gson gson;
  private final String folder;

  public MergingJsonFileLoader(MergingJsonDataLoader<B> dataLoader, Gson gson, String folder) {
    super(gson, folder);
    this.dataLoader = dataLoader;
    this.gson = gson;
    this.folder = folder;
  }

  /**
   * Loads and parses the relevant files into the data loader
   * @param mergeFolder  If nonnull, subfolder to load as a "second datapack", for testing merging behavior. If null, skips the merging
   * @param files  List of files
   */
  public void loadAndParseFiles(@Nullable String mergeFolder, ResourceLocation... files) {
    Map<ResourceLocation,List<Resource>> stacks = new HashMap<>();
    for (Entry<ResourceLocation, JsonElement> entry : loadFilesAsSplashlist(files).entrySet()) {
      addResource(stacks, entry.getKey(), entry.getValue(), "base");
    }
    if (mergeFolder != null) {
      JsonFileLoader fakeSecondDataPack = new JsonFileLoader(gson, folder + "/" + mergeFolder);
      for (Entry<ResourceLocation, JsonElement> entry : fakeSecondDataPack.loadFilesAsSplashlist(files).entrySet()) {
        addResource(stacks, entry.getKey(), entry.getValue(), "merge");
      }
    }
    ResourceManager manager = mock(ResourceManager.class);
    when(manager.listResourceStacks(anyString(), any())).thenReturn(stacks);
    dataLoader.onResourceManagerReload(manager);
  }

  private void addResource(Map<ResourceLocation,List<Resource>> stacks, ResourceLocation id, JsonElement json, String packId) {
    ResourceLocation path = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath() + ".json");
    Resource resource = mock(Resource.class);
    try {
      when(resource.openAsReader()).thenAnswer(ignored -> new BufferedReader(new StringReader(json.toString())));
    } catch (IOException e) {
      throw new AssertionError("Failed to configure mocked datapack resource", e);
    }
    when(resource.sourcePackId()).thenReturn(packId);
    stacks.computeIfAbsent(path, ignored -> new ArrayList<>()).add(resource);
  }

  /** Loads material files while preserving the typed material IDs used by the production API. */
  public void loadAndParseFiles(@Nullable String mergeFolder, MaterialId... files) {
    ResourceLocation[] locations = new ResourceLocation[files.length];
    for (int i = 0; i < files.length; i++) {
      locations[i] = files[i].location();
    }
    loadAndParseFiles(mergeFolder, locations);
  }
}
