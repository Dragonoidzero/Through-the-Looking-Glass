package azzy.fabric.lookingglass.util.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface TexManRegEdit {
    void unregisterTexture(final Identifier id);
}
