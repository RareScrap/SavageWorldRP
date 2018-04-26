package rsstats.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import rsstats.common.RSStats;
import rsstats.inventory.container.UpgradeContainer;


/**
 * GUI для апгрейда вещей, через присваивание им модификаторов. Имеет разные текстуры лдя консольной
 * (т.е. вызванной консольной командой) и блочной версии (через ПКМ по блоку {@link rsstats.inventory.container.rsstats.blocks.UpgradeStationBlock}).
 */
public class UpgradeGUI extends GuiContainer {
    /*private static final ResourceLocation background =
            new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window.png");*/

    // TODO: Должно быть статиком и/или финалом??
    /** Расположение фона GUI */
    private ResourceLocation background;

    public UpgradeGUI(Container container) {
        super(container);

        // Высталяем размеры контейнера. Соответствует размерам GUI на текстуре.
        UpgradeContainer upgradeContainer = (UpgradeContainer) container;
        if (upgradeContainer.isCalledFromBlock()) {
            this.xSize = 176;
            this.ySize = 224;
            background = new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window.png");
        } else {
            this.xSize = 175;
            this.ySize = 165;
            background = new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window_cmd.png");
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        // Отрисовываем текстуру GUI
        //drawTexturedRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize, xSize, ySize);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        //drawTexturedRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize, xSize, ySize);
    }
}
