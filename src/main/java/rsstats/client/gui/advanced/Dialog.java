package rsstats.client.gui.advanced;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import rsstats.client.gui.MainMenuGUI;
import rsstats.common.RSStats;

import java.util.List;

/**
 * Диалоговое окно, способное отображаться поверх другого {@link GuiScreen}
 *
 * <p>Внимание! Не отображайте это GUI, используя:
 * <ul>
 *     <li>Minecraft.getMinecraft().displayGuiScreen(new Dialog());</li>
 *     <li>player.getEntityPlayer().openGui(RSStats.instance, RSStats.DIALOG_GUI_CODE, player.getEntityPlayer().worldObj, (int) player.getEntityPlayer().posX, (int) player.getEntityPlayer().posY, (int) player.getEntityPlayer().posZ);</li>
 *     <li>FMLClientHandler.instance().displayGuiScreen(this.player.getEntityPlayer(), new Dialog());</li>
 * </ul>
 * т.к. это закрвает предыдущее GUI и ваш диалог будет отрисован как новое окно, а не поверх старого.
 * </p>
 */
public class Dialog extends GuiScreen {
    /** Текстура диалогового окна */
    private static final ResourceLocation background =
            new ResourceLocation(RSStats.MODID,"textures/gui/dialog.png");
    /** Отрисовщик текста */
    protected ZLevelFontRenderer fontRenderer = new ZLevelFontRenderer(
            Minecraft.getMinecraft().gameSettings,
            new ResourceLocation("textures/font/ascii.png"),
            Minecraft.getMinecraft().renderEngine,
            false);

    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    public int guiLeft;
    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    public int guiTop;
    /** Родитель, поверх которого вызывается диалоговое окно. */
    GuiScreen parent;
    int parentGuiLeft;
    int parentGuiTop;
    int parentXSize;
    int parentYSize;

    ZLevelGuiButton positiveButton;
    ZLevelGuiButton negativeButton;
    ZLevelGuiButton cancelButton;


    public Dialog(GuiScreen parent, int parentXSize, int parentYSize, int parentGuiLeft, int parentGuiTop) {
        // Высталяем размеры GUI. Соответствует размерам GUI на текстуре.
        width = 228;
        height = 64;
        this.mc = Minecraft.getMinecraft();
        this.zLevel = MainMenuGUI.DialogZLevel; // TODO: Перенести в initGui

        this.parent = parent;
        this.parentGuiLeft = parentGuiLeft;
        this.parentGuiTop = parentGuiTop;
        this.parentXSize = parentXSize;
        this.parentYSize = parentYSize;
    }

    @Override
    public void initGui() {
        positiveButton = new ZLevelGuiButton(0, guiLeft+6, guiTop+35, 70, 20, StatCollector.translateToLocal("gui.MainMenu.CloseDialog.positive"));
        negativeButton = new ZLevelGuiButton(1, guiLeft+79, guiTop+35, 70, 20,StatCollector.translateToLocal("gui.MainMenu.CloseDialog.negative"));
        cancelButton = new ZLevelGuiButton(2, guiLeft+152, guiTop+35, 70, 20,StatCollector.translateToLocal("gui.MainMenu.CloseDialog.cancel"));

        positiveButton.setZLevel(this.zLevel);
        negativeButton.setZLevel(this.zLevel);
        cancelButton.setZLevel(this.zLevel);

        if (!buttonList.isEmpty()) {
            buttonList.clear(); // TODO: Есть подозрение, что это никогда не вызовется
        }

        buttonList.add(positiveButton);
        buttonList.add(negativeButton);
        buttonList.add(cancelButton);

    }

    public List getButtonList() {
        return buttonList;
    }

    /**
     * ДОЛЖЕН вызываться в {@link GuiScreen#drawScreen(int, int, float)} родителя!
     * Метод-обертка для {@link #drawScreen(int, int, float)},
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_, int parentXSize, int parentYSize, int parentGuiLeft, int parentGuiTop) {
        // Централизируем GUI диалога
        guiLeft = (parentXSize - width)/2 + parentGuiLeft;
        guiTop = (parentYSize - height)/2 + parentGuiTop;

        // Обновляем корд кнопок // TODO: Зачем?
        //buttonList.clear(); // TODO: НО БЛЯТЬ НЕ ТАК ЖЕ ГРУБО!
        //initGui();
        positiveButton.xPosition = guiLeft+6;
        positiveButton.yPosition = guiTop+35;
        negativeButton.xPosition = guiLeft+79;
        negativeButton.yPosition = guiTop+35;
        cancelButton.xPosition = guiLeft+152;
        cancelButton.yPosition = guiTop+35;

        drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    /**
     * Не вызывайте этот метод напрямую. Используйте обертку {@link #drawScreen(int, int, float, int, int, int, int)}
     */
    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        //drawDefaultBackground(); // TODO: Не срабатвает
        //this.drawGradientRectZLevel(0, 0, 1000/*this.width*/, 1000/*this.height*/, -1072689136, -804253680, Dialog.this.zLevel + 5000);

        this.mc.getTextureManager().bindTexture(background);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, width, height);

        // Увеличиваем zLevel текста, чтоб тот отрисовывался над кнопкой и рисуем строку
        fontRenderer.zLevel = zLevel + 1;
        fontRenderer.drawString(StatCollector.translateToLocal("gui.MainMenu.CloseDialog"),
                guiLeft+31,
                guiTop+15,
                0x444444,
                false);

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    /**
     * Обертка для {@link #actionPerformed(GuiButton)}, которую можно вызвать из родительского GUI
     * @param guiButton Кнопка, по которой было сделано действие.
     */
    public void handleAction(GuiButton guiButton) {
        actionPerformed(guiButton);
    }

    /**
     * @see #handleAction(GuiButton)
     */
    @Override
    protected void actionPerformed(GuiButton guiButton) {
        switch (guiButton.id) {
            case 0: {
                positiveActionPerformed();
                break;
            }
            case 1: {
                negativeActionPerformed();
                break;
            }
            case 2: {
                cancelActionPerformed();
                break;
            }
        }
    }

    /**
     * Вызывается в ответ на нажатие {@link #positiveButton}. По умолчанию, закрывает текущее
     * открытое GUI.
     */
    public void positiveActionPerformed() {
        // Закрываем GUI
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    /**
     * Вызывается в ответ на нажатие {@link #negativeButton}.
     */
    public void negativeActionPerformed() {

    }

    /**
     * Вызывается в ответ на нажатие {@link #cancelButton}. По умолчанию, закрывает текущее
     * открытое GUI.
     */
    public void cancelActionPerformed() {
        // Закрываем GUI
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
