package rsstats.client.gui.advanced;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * {@link FontRenderer} с настраиваемой высотой отрисовки.
 */
public class ZLevelFontRenderer extends FontRenderer {
    // cм родителя. Скопировано из-за private
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    public float zLevel;

    public ZLevelFontRenderer(GameSettings gameSettings, ResourceLocation resourceLocation, TextureManager textureManager, boolean unicodeFlag) {
        super(gameSettings, resourceLocation, textureManager, unicodeFlag);
    }

    // Копипаст из родителя из-за private
    private ResourceLocation getUnicodePageLocation(int p_111271_1_)
    {
        if (unicodePageLocations[p_111271_1_] == null)
        {
            unicodePageLocations[p_111271_1_] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", new Object[] {Integer.valueOf(p_111271_1_)}));
        }

        return unicodePageLocations[p_111271_1_];
    }

    // Копипаст из родителя с добавлено поддержкой zLevel
    @Override
    protected float renderUnicodeChar(char p_78277_1_, boolean p_78277_2_) {
        if (this.glyphWidth[p_78277_1_] == 0)
        {
            return 0.0F;
        }
        else
        {
            int i = p_78277_1_ / 256;

            //this.loadGlyphTexture(i); // Из родителя
            bindTexture(this.getUnicodePageLocation(i)); // Мой реализация loadGlyphTexture() в обход кодительского private

            int j = this.glyphWidth[p_78277_1_] >>> 4;
            int k = this.glyphWidth[p_78277_1_] & 15;
            float f = (float)j;
            float f1 = (float)(k + 1);
            float f2 = (float)(p_78277_1_ % 16 * 16) + f;
            float f3 = (float)((p_78277_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_78277_2_ ? 1.0F : 0.0F;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, zLevel);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, zLevel);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, zLevel);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, zLevel);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }
}
