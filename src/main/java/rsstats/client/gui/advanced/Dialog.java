package rsstats.client.gui.advanced;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import rsstats.client.gui.MainMenuGUI;
import rsstats.common.RSStats;

/**
 * Диалоговое окно, способное отображаться поверх другого {@link GuiScreen}
 *
 * <p><strong>Внимание!</strong> Не отображайте это GUI, используя:
 * <ul>
 *     <li>Minecraft.getMinecraft().displayGuiScreen(GuiScreen guiScreen);</li>
 *     <li>player.getEntityPlayer().openGui(Object mod, int modGuiId, World world, int x, int y, int z);</li>
 *     <li>FMLClientHandler.instance().displayGuiScreen(EntityPlayer player, GuiScreen gui);</li>
 * </ul>
 * т.к. это закрвает предыдущее GUI и ваш диалог будет отрисован как новое окно, а не поверх старого.
 *
 *
 * <p>
 *     Чтобы диалог заработал, из gui-родителя, в родителе необходимо вызывать следующие методы:
 *     <ul>
 *         <li>{@link #initGui()} нужно вызвать в initGui() GUI-родителя</li>
 *         <li>{@link #drawScreen(int, int, float)} нужно вызвать в drawScreen() GUI-родителя, проверя флаг.
 *         Где флаг - обычная boolean переменная, которая true, если диалог следует показать на экране, и false,
 *         если диалог скрыт. Пример:
 *         <pre>
 *           <code>
 *               if (isPlayerTryExitWhileEditStats) {
 *                  dialog.drawScreen(mouseX, mouseY, partialTicks);
 *               }
 *           </code>
 *           </pre>
 *         </li>
 *         <li>{@link #handleMouseInput()} нужно вызвать в handleMouseInput() GUI-родителя, проверя флаг.
 *         Если вы попытаетесь вызвать этот метод, не окружив ее if'ом, то нажимать на кнопки диалога можно будет
 *         даже тогда, когда диалога нет на экране.</li>
 *     </ul>
 *     Под родителем понимается не родительский класс, а о GUI, поверх которого будет отображаться
 *     диалог
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

    /** The X size of the inventory window in pixels. */
    protected int xSize = 228; // Размеры GUI. Соответствует размерам GUI на текстуре.
    /** The Y size of the inventory window in pixels. */
    protected int ySize = 64;
    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    public int guiLeft;
    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    public int guiTop;
    /** Родитель, поверх которого вызывается диалоговое окно. */
    protected GuiScreen parent;

    protected ZLevelGuiButton positiveButton;
    protected ZLevelGuiButton negativeButton;
    protected ZLevelGuiButton cancelButton;


    public Dialog(GuiScreen parent) {
        this.mc = Minecraft.getMinecraft();
        this.parent = parent;
    }

    // Minecraft спользует этот метод как для добавления контролов, так и для настройки самого GUI (полей, например)
    // ВНИМАНИЕ! Для правильной работы диалога вы обязаны вызвать этот метод из одноименного метода в parent'те
    @Override
    public void initGui() {
        this.zLevel = MainMenuGUI.DialogZLevel;
        // Устанавливаем размер окна диалога, такой же как у родителя
        this.width = parent.width;
        this.height = parent.height;

        /* Централизируем GUI диалога
         * Нет необходимости вычислять guiLeft и guiTop в drawScreen(), т.к.
         * необходимость в пересчете этих значений есть только при изменении размера экрана.
         * Игра автоматически вызывает initGui(), для пересчета необходимых параметров.
         */
        guiLeft = (this.width - this.xSize) / 2;
        guiTop = (this.height - this.ySize) / 2;

        positiveButton = new ZLevelGuiButton(0, guiLeft+6, guiTop+35, 70, 20, StatCollector.translateToLocal("gui.MainMenu.CloseDialog.positive"));
        negativeButton = new ZLevelGuiButton(1, guiLeft+79, guiTop+35, 70, 20,StatCollector.translateToLocal("gui.MainMenu.CloseDialog.negative"));
        cancelButton = new ZLevelGuiButton(2, guiLeft+152, guiTop+35, 70, 20,StatCollector.translateToLocal("gui.MainMenu.CloseDialog.cancel"));

        positiveButton.setZLevel(this.zLevel);
        negativeButton.setZLevel(this.zLevel);
        cancelButton.setZLevel(this.zLevel);

        /*
         * Хотя сама игра и очищает buttonList, возможна ситуация, когда
         * initGui() был вызван вручную. В этом случае buttonList может быть не пустым
         */
        if (!buttonList.isEmpty()) {
            buttonList.clear();
        }

        buttonList.add(positiveButton);
        buttonList.add(negativeButton);
        buttonList.add(cancelButton);

    }

    // TODO: Сделать так, чтобы бэкграунд отрисовывался прямо в диалоге, а не в гуи-родителе
    /**
     * ДОЛЖЕН вызываться в {@link GuiScreen#drawScreen(int, int, float)} родителя!
     */
    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        //drawDefaultBackground(); // TODO: Не срабатвает
        //this.drawGradientRectZLevel(0, 0, 1000/*this.width*/, 1000/*this.height*/, -1072689136, -804253680, Dialog.this.zLevel + 5000);

        this.mc.getTextureManager().bindTexture(background);

        // debug
        /*
         * Эта проверка никогда не пройдет, что подтверждает бессмысленность перерасчета guiLeft и
         * guiTop в drawScreen. Это следует делать в initGui()
         */
        /*if (guiLeft != (this.width - this.xSize) / 2) {
            System.out.println("TEST");
        }*/

        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);

        // Увеличиваем zLevel текста, чтоб тот отрисовывался над кнопкой и рисуем строку
        fontRenderer.zLevel = zLevel + 1; // TODO: Перенести в initGui()
        drawCenteredString(fontRenderer,
                StatCollector.translateToLocal("gui.MainMenu.CloseDialog"),
                guiLeft + (xSize / 2),
                guiTop + 15,
                0x444444);

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

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

    // Не нужно переопределять drawGradientRect ради установки своего zLevel, т.к. тот используется не только для бэкграунда
    /**
     * Отрисовывает фон позади диалога, схожий с тем, что отрисовывается при помощи
     * {@link GuiScreen#drawDefaultBackground()}
     */
    public void drawDialogGradientBackground(int x, int y) {
        DrawUtils.drawGradientRectZLevel(x, y, width, height, -1072689136, -804253680, zLevel - 1);
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
