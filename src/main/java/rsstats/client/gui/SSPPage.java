package rsstats.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.container.StatsContainer;
import rsstats.inventory.slots.StatSlot;

/**
 * UI инвентаря статов, скиллов и перков
 * @author RareScrap
 */
public class SSPPage extends GuiContainer {
    /** x size of the inventory window in pixels. Defined as float, passed as int */
    private float xSize_lo;

    /** y size of the inventory window in pixels. Defined as float, passed as int. */
    private float ySize_lo;

    /** Текстура-фон */
    public static final ResourceLocation background = 
			new ResourceLocation("rsstats","textures/gui/SSPPage.png");
    
    // Я пока не использую бекграундные иконки
    /** Normally I use '(ModInfo.MOD_ID, "textures/...")', but it can be done this way as well */
    //private static final ResourceLocation iconLocation = new ResourceLocation("tutorial:textures/gui/custom_inventory.png");

    /** Could use IInventory type to be more generic, but this way will save an import... */
    private final StatsInventory statsInventory;

    public SSPPage(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory) {
        super(new StatsContainer(player, inventoryPlayer, statsInventory));
        System.out.print("SSPPage(...)\n");
        this.statsInventory = statsInventory;
        // if you need the player for something later on, store it in a local variable here as well
    }

    /**
     * Draws the screen and all the components in it.
     * @param mouseX TODO
     * @param mouseY TODO
     * @param f TODO
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        xSize_lo = mouseX;
        ySize_lo = mouseY;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     * @param mouseX TODO
     * @param mouseY TODO
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // This method will simply draw inventory names on the screen - you could do without it entirely
        // if that's not important to you, since we are overriding the default inventory rather than
        // creating a specific type of inventory

        String s = this.statsInventory.hasCustomInventoryName()? this.statsInventory.getInventoryName() : /*I18n.getString(this.inventory.getInvName())*/ "PIDOR";
        
        // Текст я пока не рендерю
        // with the name "Custom Inventory", the 'Cu' will be drawn in the first slot
        //this.fontRenderer.drawString(s, this.xSize - this.fontRenderer.getStringWidth(s), 12, 4210752);
        // this just adds "Inventory" above the player's inventory below
        //this.fontRenderer.drawString(I18n.getString("container.inventory"), 80, this.ySize - 96, 4210752);
    }

    /**
    * Draw the background layer for the GuiContainer (everything behind the items)
    */
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //GL11.glScalef(2.0F, 2.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        
        this.xSize = 340/2;
        this.ySize = 212/2;
        
        int k = this.guiLeft/2 - xSize/4;
        int l = this.guiTop/2 - ySize/4;
        
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        
        
        /*for (int i = 0; i < statsInventory.getSizeInventory(); i++) {
            StatSlot slot = statsInventory.
            if (slot.getHasStack() && slot.getSlotStackLimit()==1) {
            	this.drawTexturedModalRect(k+slot.xDisplayPosition, l+slot.yDisplayPosition, 200, 0, 16, 16);
            }
        }*/
        
        // думаю, что это рендер предметов в инвентаре
        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
            if (slot.getHasStack() && slot.getSlotStackLimit()==1)
            {
            	this.drawTexturedModalRect(k+slot.xDisplayPosition, l+slot.yDisplayPosition, 200, 0, 16, 16);
            }
        }
        
        
        
        // ЭТО ИЗ ТУТОРА
        /*GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //mc.getTextureManager().bindTexture(iconLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawPlayerModel(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - xSize_lo, guiTop + 25 - ySize_lo, mc.thePlayer);
        */
    }
    
    /**
     * Copied straight out of vanilla - renders the player model on screen
     */
    public static void drawPlayerModel(int x, int y, int scale, float yaw, float pitch, EntityLivingBase entity) {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 50.0F);
        GL11.glScalef(-scale, scale, scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        //RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
        entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
        entity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        //RenderManager.instance.playerViewY = 180.0F;
        //RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        //GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        //OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
