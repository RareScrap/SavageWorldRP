package rsstats.client.gui;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketShowSkillsByStat;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import rsstats.items.SkillItem;

import java.util.Timer;

/**
 * GUI для основного окна мода, содержащее информацию о персонаже (имя, уровень, здоровье, защита, харизма,
 * стойкость), панель предметов и панели статов, навыков и перков.
 * @author RareScrap
 */
public class MainMenuGUI extends InventoryEffectRenderer {
    /** Расположение фона GUI */
    private static final ResourceLocation background =
            new ResourceLocation(RSStats.MODID,"textures/gui/StatsAndInvTab_FIT.png");

    /** Период обновления экрана в мс */
    private static final int UPDATE_PERIOD = 100;
    /** Игрок, открывший GUI */
    public ExtendedPlayer player;
    /** UnlocalozedName текущей выбранно статы */
    private String currentStat = "";

    /** Инвентарь для статов */
    // Could use IInventory type to be more generic, but this way will save an import...
    // Нужно для запроса кастомного имени инвентаря для отрисоки названия инвентаря
    //private final StatsInventory statsInventory;

    /** Таймер, выполняющий перерасчет параметров {@link ExtendedPlayer}'ра на стороне клиента.
     * Для этих целей можно использовать и пакет, который будет слаться при клике/заполнеии слота, но
     * зачем, когда можно обойтись и без пакета?*/
    private Timer timer;


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
    protected void renderToolTip(ItemStack itemStack, int p_146285_2_, int p_146285_3_) {
        Item item = itemStack.getItem();
        if (!item.getUnlocalizedName().equals(currentStat) && !(item instanceof SkillItem)) {
            PacketShowSkillsByStat packet = new PacketShowSkillsByStat(itemStack.getItem().getUnlocalizedName());
            CommonProxy.INSTANCE.sendToServer(packet);
            currentStat = itemStack.getItem().getUnlocalizedName();
        }

        super.renderToolTip(itemStack, p_146285_2_, p_146285_3_);
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
        super.keyTyped(p_73869_1_, p_73869_2_);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events.
     */
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        //timer.cancel();
        //timer.purge();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        super.initGui();
        /*timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                player.updateParams();
                updateScreen();
            }
        }, 0, UPDATE_PERIOD);*/
    }
}
