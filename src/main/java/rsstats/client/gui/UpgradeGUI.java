package rsstats.client.gui;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import rsstats.blocks.UpgradeStationBlock;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketSyncGUI;
import rsstats.inventory.container.UpgradeContainer;
import rsstats.utils.DescriptionCutter;

import java.util.List;


/**
 * GUI для апгрейда вещей, через присваивание им модификаторов. Имеет разные текстуры лдя консольной
 * (т.е. вызванной консольной командой) и блочной версии (через ПКМ по блоку {@link UpgradeStationBlock}).
 */
public class UpgradeGUI extends GuiContainer implements ICrafting {
    /*private static final ResourceLocation background =
            new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window.png");*/

    // TODO: Должно быть статиком и/или финалом??
    /** Расположение фона GUI */
    private ResourceLocation background;
    /** Контейнер GUI */
    private UpgradeContainer upgradeContainer;
    /** Поле ввода описания улучшения */
    private GuiTextField descriptionTextField;
    private GuiTextField valueTextField;

    public UpgradeGUI(Container container) {
        super(container);

        // Высталяем размеры контейнера, в зависимости от способа вызова. Соответствует размерам GUI на текстуре.
        upgradeContainer = (UpgradeContainer) container;
        if (upgradeContainer.isCalledFromBlock()) { // GUI вызывается при ПКМ на блок
            this.xSize = 176;
            this.ySize = 224;
            background = new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window.png");
        } else { // GUI вызывается командой консоли
            this.xSize = 176;
            this.ySize = 165;
            background = new ResourceLocation(RSStats.MODID,"textures/gui/upgrade_window_cmd.png");
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        // Отрисовываем текстуру GUI
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        // Расчитываем координаты текстур полей, для вызова из консоли или из блока
        UpgradeContainer container = (UpgradeContainer) this.inventorySlots;
        int fieldsTextureY;
        if (container.isCalledFromBlock()) {
            fieldsTextureY = 105;
        } else {
            fieldsTextureY = 75; // TODO: странный баг. Если игра не на весь экран - видны красные текстуры-заполнители. Сдвину значение до 76 - Все наоборот: теперь заполнители видны в полноэкранном режиме, но не видны в маленьком
        }

        // Отрисовываем активную/неактивную текстуру текстого поля description поверх цвета-маркера
        if (container.isTextFieldAvailable()) {
            this.drawTexturedModalRect(this.width / 2 - 29, this.height/2-fieldsTextureY, 2, 239, 110, 16);
            this.drawTexturedModalRect(this.width / 2 - 49, this.height/2-fieldsTextureY, 200, 205, 19, 16);
        } else {
            this.drawTexturedModalRect(this.width / 2 - 29, this.height/2-fieldsTextureY, 113, 239, 110, 16); // Не расчитывай начало текстур относительно xSize и ySize. Расчитывай отностительно начала текстуры.
            this.drawTexturedModalRect(this.width / 2 - 49, this.height/2-fieldsTextureY, 220, 205, 19, 16);
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        // Расчитываем координаты текстовых полей, для вызова из консоли или из блока
        UpgradeContainer container = (UpgradeContainer) this.inventorySlots;
        int fieldsY;
        if (container.isCalledFromBlock()) {
            fieldsY = 102;
        } else {
            fieldsY = 72;
        }

        Keyboard.enableRepeatEvents(true); // Позволяет зажать клавишу и напечатать "аааааааа"
        descriptionTextField = new GuiTextField(this.fontRendererObj,  this.width / 2 - 26, this.height/2-fieldsY, 101, 16);
        this.descriptionTextField.setFocused(false); // Нам не нужен фокус на момент открытия
        this.descriptionTextField.setTextColor(-1);
        this.descriptionTextField.setDisabledTextColour(-1);
        this.descriptionTextField.setEnableBackgroundDrawing(false);
        this.descriptionTextField.setMaxStringLength(40);

        valueTextField = new GuiTextField(this.fontRendererObj,  this.width / 2 - 47, this.height/2-fieldsY, 12, 16); // 105
        this.valueTextField.setFocused(false); // Нам не нужен фокус на момент открытия
        this.valueTextField.setTextColor(-1);
        this.valueTextField.setDisabledTextColour(-1);
        this.valueTextField.setEnableBackgroundDrawing(false);
        this.valueTextField.setMaxStringLength(4);

        this.inventorySlots.removeCraftingFromCrafters(this); // TODO: ХЗ зачем это. Кажется всякое GUI с крафтом должно делать это
        this.inventorySlots.addCraftingToCrafters(this);

        super.initGui();
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     *
     * @param par1 Нажатая буква с учетом раскладки клавиатуры
     * @param par2 ASCII код буквы
     */
    @Override
    protected void keyTyped(char par1, int par2) {
        if (descriptionTextField.isFocused() || valueTextField.isFocused()) { // Окно можно закрыть только если поля не в фокусе
            this.descriptionTextField.textboxKeyTyped(par1, par2); // Добавляем букву в поле. Метод сам проверит фокус.
            if ((par1 >= 48 && par1 <= 57) || par1 == 45 || par1 == 43 || par1 == 8) { // Допустимые символы: 0-9, стереть и +-
                this.valueTextField.textboxKeyTyped(par1, par2);
            }

            // Получаем значение модификатора
            int value = 0;
            if (!valueTextField.getText().isEmpty()) { // Условие, дабы не использовать затратный try слишком часто
                try {
                    value = Integer.parseInt(valueTextField.getText());
                } catch (NumberFormatException e) {
                }
            }

            // Форматируем вид текста
            String formatCode;
            if (value > 0) {
                formatCode = "\u00A7" + String.valueOf(2); // Символ §
            } else {
                formatCode = "\u00A7" + String.valueOf(4);
            }
            String formattedDescription = DescriptionCutter.formatEveryWord(descriptionTextField.getText(), formatCode);

            this.upgradeContainer.updateFields(formattedDescription, value); // Обновим описание модификатора в контейнере клиента (кажется)
            CommonProxy.INSTANCE.sendToServer(new PacketSyncGUI(descriptionTextField.getText(), value)); // Отошлем ввод игрока серверу, чтоб тот мог сгенерить предмет
        } else {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        super.updateScreen();
        this.descriptionTextField.updateCursorCounter();
        this.valueTextField.updateCursorCounter();

        // Выставляем активность текстого поля в зависимости от его доступности
        UpgradeContainer container = (UpgradeContainer) this.inventorySlots;
        if (container.isTextFieldAvailable()) {
            descriptionTextField.setEnabled(true);
            valueTextField.setEnabled(true);
        } else {
            descriptionTextField.setEnabled(false);
            valueTextField.setEnabled(false);
            descriptionTextField.setText("");
            valueTextField.setText("");
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();

        super.drawScreen(par1, par2, par3);
        this.descriptionTextField.drawTextBox();
        this.valueTextField.drawTextBox();
    }

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int x, int y, int btn) {
        super.mouseClicked(x, y, btn);
        this.descriptionTextField.mouseClicked(x, y, btn);
        this.valueTextField.mouseClicked(x, y, btn);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.inventorySlots.removeCraftingFromCrafters(this);
    }

    @Override
    public void sendContainerAndContentsToPlayer(Container p_71110_1_, List p_71110_2_) {

    }

    /**
     * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
     * contents of that slot. Args: Container, slot number, slot contents
     *
     * @param container
     * @param slotNumber
     * @param itemStack
     */
    @Override
    public void sendSlotContents(Container container, int slotNumber, ItemStack itemStack) {
        // вставить название блока как в наковальне
        /*UpgradeContainer upgradeContainer = (UpgradeContainer) this.inventorySlots;
        //if (!upgradeContainer.isTextFieldAvailable())
        if (itemStack != null) {
            //this.descriptionTextField.setText(itemStack == null ? "" : itemStack.getDisplayName());
            descriptionTextField.setEnabled(true);
            this.descriptionTextField.setText(descriptionTextField.getText() + " " +itemStack.getDisplayName());
            updateScreen();
        }*/
    }

    /**
     * Sends two ints to the client-side Container. Used for furnace burning time, smelting progress, brewing progress,
     * and enchanting level. Normally the first int identifies which variable to update, and the second contains the new
     * value. Both are truncated to shorts in non-local SMP.
     *
     * @param p_71112_1_
     * @param p_71112_2_
     * @param p_71112_3_
     */
    @Override
    public void sendProgressBarUpdate(Container p_71112_1_, int p_71112_2_, int p_71112_3_) {

    }
}
