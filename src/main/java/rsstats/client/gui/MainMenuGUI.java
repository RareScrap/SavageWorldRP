package rsstats.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import rsstats.client.gui.advanced.AdvanceInventoryEffectRenderer;
import rsstats.client.gui.advanced.Dialog;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketDialogAction;
import rsstats.common.network.PacketShowSkillsByStat;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.container.MainContainer;
import rsstats.inventory.tabs_inventory.TabHostInventory;
import rsstats.items.SkillItem;

/**
 * GUI для основного окна мода, содержащее информацию о персонаже (имя, уровень, здоровье, защита, харизма,
 * стойкость), панель предметов и панели статов, навыков и перков.
 * @author RareScrap
 */
public class MainMenuGUI extends AdvanceInventoryEffectRenderer {
    /** Расположение фона GUI */
    private static final ResourceLocation background =
            new ResourceLocation(RSStats.MODID,"textures/gui/StatsAndInvTab_FIT.png");

    // TODO: Удалить ненужные константы
    public static final float MainMenuGUIZLevel = 5.0F;
    public static float DialogZLevel = 400.0F;
    public static float DialogBacgroundZLevel = 300.0F;

    /** Игрок, открывший GUI */
    public ExtendedPlayer player;
    /** UnlocalozedName текущей выбранно статы */
    private String currentStat = "";
    /** Флаг, обозначающий намерение игрока закрыть окно, пока он все прокачивает статы/навыки */
    private boolean isPlayerTryExitWhileEditStats = false;
    /** Диалог закрытия окна */
    private Dialog exitDialog;

    /** Инвентарь для статов */
    // Could use IInventory type to be more generic, but this way will save an import...
    // Нужно для запроса кастомного имени инвентаря для отрисоки названия инвентаря
    //private final StatsInventory statsInventory;

    public MainMenuGUI(ExtendedPlayer player, MainContainer mainContainer) {
        super(mainContainer);
        this.allowUserInput = true;
        this.player = player;

        // Высталяем размеры контейнера. Соответствует размерам GUI на текстуре.
        this.xSize = 340;
        this.ySize = 211;
        // Выставляем края контейнера (верхний и левый)
        this.guiLeft = this.width/2 - xSize/2;
        this.guiTop = this.height/2 - ySize/2;
    }

    /**
     * Свой аналог {@link #drawTexturedModalRect(int, int, int, int, int, int)}, способный работать с текстурами
     * разрешением более чем 256x256.
     * @param x Координата начала отрисовки относительно левого-верхнего угла экрана игрока
     * @param y Координата начала отрисовки относительно левого-верхнего угла экрана игрока
     * @param u Координата начала текстуры по оси X относительно левого-верхнего угла текстуры
     * @param v Координата начала текстуры по оси Y относительно левого-верхнего угла текстуры
     * @param width Ширина текстуры, которую нужно отрисовать
     * @param height Высота текстуры, которую нужно отрисовать
     * @param textureWidth Общая ширина текстуры (кол-во пикселей в файле)
     * @param textureHeight Общая высота текстуры (кол-во пикселей в файле)
     */
    // Взято отсюда: http://www.minecraftforge.net/forum/topic/20177-172-gui-cant-more-than-256256/
    private void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
        tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
        tessellator.draw();
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     * @param partialTicks
     * @param mouseX
     * @param mouseY
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //GL11.glScalef(2.0F, 2.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        // Отрисовываем текстуру GUI
        drawTexturedRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize, xSize, ySize);
        // Орисовываем превью игрока
        drawPlayerModel(this.guiLeft+30, this.guiTop+90, /*17*/ 40, (float)(this.guiLeft + 51) - mouseX, (float)(this.guiTop + 75 - 50) - mouseY, this.mc.thePlayer);

        // Это было в туторах, но я хз на что это влияет. Слоты и рендер предметов работают и без этого
        /*for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
            //if (slot.getHasStack() && slot.getSlotStackLimit()==1)
            //{
            	this.drawTexturedModalRect(k+slot.xDisplayPosition, l+slot.yDisplayPosition, 200, 0, 16, 16);
            //}
        }*/

        /* ВНИМАНИЕ! При выборе метода для отрисовки между drawGuiContainerBackgroundLayer и
         * drawGuiContainerForegroundLayer следует помнить, что при неправильном порядке отрисовке
         * некоторые элементы не пройдут GL_DEPTH_TEST и не отрендерятся.
         * См. https://forum.mcmodding.ru/threads/Не-могу-отрисовать-свой-drawdefaultbackground.21455/
         */
        if (isPlayerTryExitWhileEditStats) {
            // Например, тут не следует рендерить бэкграунд диалогового окна, т.к. итемстаки не пройдут GL_DEPTH_TEST
            //drawGradientRectZLevel(0, 0, 1000/*this.width*/, 1000/*this.height*/, -1072689136, -804253680, MainMenuGUI.DialogBacgroundZLevel);

            // А вот отрисовать текстуру диалогового окна можно
            exitDialog.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     *
     * @param p_146979_1_
     * @param p_146979_2_
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        int textY = 123;
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.step", player.getStep()), 8, textY, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.level", player.getLvl()), 60, textY, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.protection", player.getProtection()), 8, textY+=10, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.ExpPoints", player.getExp()), 60, textY, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.persistence", player.getPersistence()), 8, textY+=10, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.tiredness", player.getTiredness()), 60, textY, 0x444444, false);
        mc.fontRenderer.drawString(StatCollector.translateToLocalFormatted("gui.charisma", player.getCharisma()), 8, textY+=10, 0x444444, false);

        super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);

        /*
         * Бэкграунд диалогового окна отрисовывается в drawGuiContainerForegroundLayer, т.к. должен быть
         * отрисован после итемстаков. В противном случае они не пройдут GL_DEPTH_TEST.
         */
        if (isPlayerTryExitWhileEditStats) {
            /* Т.к. перед вызывом drawGuiContainerForegroundLayer в супер-методе drawScreen
             * происходит смещение на guiLeft вправо и guiTop вниз, то нужно восстановить нормальное смещение.
             */
            GL11.glTranslatef((float) -guiLeft, (float) -guiTop, 0.0F);
            /* Лучше способа это сделать я пока не знаю, но это гораздо лучше, чем вызывать
             * exitDialog.drawDialogGradientBackground(-guiLeft, -guiTop), т.к. в этом случае при слишком маленьком
             * экране игры, когда верхний левый уголок текстуры GUI окажется за экраном, начнутся баги с отрисовкой
             * бэкграуда диалога в виде того, что бэкграунд отображается лишь частично.
             */

            exitDialog.drawDialogGradientBackground(0, 0); // Само диалоговое окно отрисовывается в drawGuiContainerBackgroundLayer
        }
    }

    /**
     * Отрисовывает превью игрока
     * @param x TODO
     * @param y TODO
     * @param scale Маштаб модели
     * @param yaw TODO
     * @param pitch TODO
     * @param playerdrawn TODO
     */
    private static void drawPlayerModel(int x, int y, int scale, float yaw, float pitch, EntityLivingBase playerdrawn) {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 50.0F);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = playerdrawn.renderYawOffset;
        float f3 = playerdrawn.rotationYaw;
        float f4 = playerdrawn.rotationPitch;
        float f5 = playerdrawn.prevRotationYawHead;
        float f6 = playerdrawn.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float)Math.atan((double)(pitch / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        playerdrawn.renderYawOffset = (float)Math.atan((double)(yaw / 40.0F)) * 20.0F;
        playerdrawn.rotationYaw = (float)Math.atan((double)(yaw / 40.0F)) * 40.0F;
        playerdrawn.rotationPitch = -((float)Math.atan((double)(pitch / 40.0F))) * 20.0F;
        playerdrawn.rotationYawHead = playerdrawn.rotationYaw;
        playerdrawn.prevRotationYawHead = playerdrawn.rotationYaw;
        GL11.glTranslatef(0.0F, playerdrawn.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(playerdrawn, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        playerdrawn.renderYawOffset = f2;
        playerdrawn.rotationYaw = f3;
        playerdrawn.rotationPitch = f4;
        playerdrawn.prevRotationYawHead = f5;
        playerdrawn.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public void handleMouseInput() {

        // GUI уже имеет дступ к этому полю. Не смсла взвать его так, когда можно взвать напрямую
        //MainContainer container = (MainContainer) player.getEntityPlayer().openContainer;

        // Mouse.getEventX() и Mouse.getEventY() возвращают сырой ввод мыши, так что нам нужно обработать его
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseZ = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        // Проходим по всем слотам в поисках того, на который мы навели курсок
        for (Object inventorySlot : inventorySlots.inventorySlots) {
            Slot slot = (Slot) inventorySlot;

            if (isMouseOverSlot(slot, mouseX, mouseZ)) {

                // Действия, если курсор наведен на статы
                if (slot.inventory instanceof StatsInventory && !(slot.inventory instanceof SkillsInventory)) { // TODO: Найти способ делать проверку только на класс без учета наследования
                    try {
                        Item item = slot.getStack().getItem();

                        if (!item.getUnlocalizedName().equals(currentStat) && !(item instanceof SkillItem)) {
                            PacketShowSkillsByStat packet = new PacketShowSkillsByStat(item.getUnlocalizedName());
                            CommonProxy.INSTANCE.sendToServer(packet);
                            currentStat = item.getUnlocalizedName();
                        }
                    } catch (NullPointerException e) {
                        System.err.println("Не удалось определить запрос вкладки.");
                    }

                }

                // Действия, если курсор наведен на прочие вкладки
                if (slot.inventory instanceof TabHostInventory) {
                    try {
                        Item item = slot.getStack().getItem();
                        ((TabHostInventory) slot.inventory).setTab(item.getUnlocalizedName());

                    } catch (NullPointerException e) {
                        System.err.println("Не удалось определить запрос вкладки.");
                    }

                }
            }
        }

        if (isPlayerTryExitWhileEditStats) {
            exitDialog.handleMouseInput(); // Обрабатываем нажатие на GUI диалога
        }
        super.handleMouseInput(); // Обрабатываем нажатие на GUI-родителе
    }

    /**
     * см родителя
     */
    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return this.func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     *
     * @param p_73869_1_
     * @param p_73869_2_
     */
    @Override
    protected void keyTyped(char p_73869_1_, int p_73869_2_) {
        // TODO: Добавь отображение скиллов по нажатой цифре

        // Проверка на нажатие ESC во время прокачки
        if (p_73869_1_ == 27 && ((MainContainer) this.player.getEntityPlayer().openContainer).isEditMode) {
            isPlayerTryExitWhileEditStats = true;
            disableSlot = true; // Отключаем реакцию слотов на наведение мыши
            shouldDrawDefaultBackground(false); // Отключаем дефолтных бэкграунд, чтоб отрисовать свой собственный с более высоким zLevel
        } else {
            super.keyTyped(p_73869_1_, p_73869_2_);
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        exitDialog = new Dialog(this) {
            @Override
            public void negativeActionPerformed() {
                MainMenuGUI.this.shouldDrawDefaultBackground(true);
                MainMenuGUI.this.disableSlot = false;
                MainMenuGUI.this.isPlayerTryExitWhileEditStats = false;
            }

            @Override
            public void cancelActionPerformed() {
                // Отбрасываем прокачку
                CommonProxy.INSTANCE.sendToServer(new PacketDialogAction(PacketDialogAction.ActionType.CANCEL));

                super.cancelActionPerformed();
            }
        };

        exitDialog.initGui();
        this.zLevel = MainMenuGUI.MainMenuGUIZLevel;
        /* Нет смысла переопределять setWorldAndResolution для отключения базового бэкграунда, если тот метод
         * уже по-дефолту вызывает initGui() без пересоздания объекта MainGUI */
        shouldDrawDefaultBackground(!isPlayerTryExitWhileEditStats);
        super.initGui();
    }
}
