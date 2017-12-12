package rsstats.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import rsstats.common.network.RollPacketToServer;
import rsstats.utils.DescriptionCutter;
import rsstats.utils.DiceRoll;

import java.util.ArrayList;
import java.util.List;
import rsstats.common.RSStats;


// Эти классы передаются в методы

/**
 * Предмет, реализующий функции статы
 * @author RareScrap
 */
public class StatItem extends Item {
    /** Общее количество уровней статы */
    private static int NUMBER_OF_LEVELS = 5;
    
    /** Хранилище иконов для каждого уровня статы */
    private IIcon[] icons = new IIcon[NUMBER_OF_LEVELS]; // хранилище иконок для всего семейства предмета
    /** Набор разных дайсов. Порядковый номер в списке обозачает уровень статы,
     * для которой будет использован дайс. */
    protected ArrayList<DiceRoll> basicRolls;
    /** Префикс, используемый игрой для нахождеия текстур мода */
    protected final String registerIconPrefix; // "rarescrap:StrenghtIcon_" например
    /** Префикс, используемый игрой для нахождеия файлов локализации мода */
    protected final String localePrefix; // "item.StrenghtStatItem" например
    /** Префикс, одинаковый для всех статов */
    protected final String generalPrefix = "item.StatItem";
    
    /**
     * Конструктор, инициализирующий свои поля
     * @param basicRolls Дайсы бросков для каждого уровня статы
     * @param unlocalizedName Нелокализированое имя мода (TODO: ХЗ для чего нужно)
     * @param registerIconPrefix Префикс, который будет использоваться игрой для нахождения текстур данного предмета
     * @param localePrefix Префикс, который будет использоваться игрой для нахождения файлов локализации данного предмета
     */
    public StatItem(ArrayList<DiceRoll> basicRolls, String unlocalizedName, String registerIconPrefix, String localePrefix) {
        // TODO: Дайсы должны задаваться через серверный конфиг
        this.basicRolls = basicRolls;
        
        // Сохранение префиксов
        this.registerIconPrefix = registerIconPrefix;
        this.localePrefix = localePrefix;
        
        // Базовая настройка
        this.setUnlocalizedName(unlocalizedName);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.tabMaterials);
        this.setHasSubtypes(true);
    }

    // Создание вкладок для режима креатива
    // TODO: Локализировать строки
    /*public static final CreativeTabs statsTab = new CreativeTabs(RSStats.MODNAME + " Stats") {
        @Override
        public Item getTabIconItem() {
            return Items.
        }
    };*/
    
    /**
     * Добавляет к предмету пояснение.
     * @param itemstack TODO: Добавить Javadoc
     * @param player TODO: Добавить Javadoc
     * @param list TODO: Добавить Javadoc
     * @param par4 TODO: Добавить Javadoc
     */
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        // Уровень статы
        int statLevel = itemstack.getItemDamage(); // Нумерация с нуля
        
        // Строка "Уровень X"
        list.add(StatCollector.translateToLocalFormatted( generalPrefix + ".level", statLevel+1) );
        
        // Строка броска (пример: "Бросок: d6+1")
        list.add(StatCollector.translateToLocal(generalPrefix + ".roll") + ": d" + basicRolls.get(statLevel).getDice());
        
        // Пустая строка-разделитель
        list.add(""); 
        
        // Дополнительная информация по кнопке Shift
        if (GuiScreen.isShiftKeyDown()) {
            String[] str = DescriptionCutter.cut(4, StatCollector.translateToLocal(localePrefix + ".description"));
            for (int i = 0; i < str.length; i++)
                list.add( str[i] );
        } else {
            list.add( StatCollector.translateToLocal(generalPrefix + ".moreInfo") );
        }
    }

    /**
     * Регистрирует иконку для каждого подтипа статы
     * @param reg TODO: Добавить Javadoc
     */
    @Override
    public void registerIcons(IIconRegister reg) {
        for (int i = 0; i < this.icons.length; ++i) {
            this.icons[i] = reg.registerIcon(registerIconPrefix + (i + 1));
        }
    }

    /**
     * Возвращает иконку, соответствующую субтипу.
     * @param meta Порядковый номер субтипа
     * @return Икнока, соответвующая субтипу
     */
    @Override
    public IIcon getIconFromDamage(int meta) {
        if (meta > this.icons.length) {
            meta = 0;
        }
        return this.icons[meta];
    }
    
    /**
     * Создает анимацию зачарования к последнему субтипу.
     * Нужно для того, чтобы красиво выделить максимальный уровень статы.
     * @param par1ItemStack TODO: Добавить Javadoc
     * @return True, если предмету нужно включить анимацию зачарования. Иначе - false.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
         //par1ItemStack.setTagInfo("ench", new NBTTagList());
         if (par1ItemStack.getItemDamage() == NUMBER_OF_LEVELS-1)
            return true;
         else
             return false;
    }

    /**
     * Создание субтипов (уровней) статы
     * @param item TODO: Добавить Javadoc
     * @param tab TODO: Добавить Javadoc
     * @param list TODO: Добавить Javadoc
     */
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < this.icons.length; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    /**
     * Возвращает нелокализированное имя предмета
     * @param stack Предмет, для которого требуется вернуть нелокализированное имя
     * @return Нелокализированное имя предмета
     */
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName();
        //return this.getUnlocalizedName() + "_" + (Integer.valueOf( stack.getItemDamage() ) + 1); - пригодится когда каждому подтипу нужно дать индивидуальное имя
    }  
    
    // Работает когда юзаешь предмет на панели
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        // If нужен, чтобы броить бросок только один раз
        //if (world.isRemote) { // TODO: На какой стороне вычисляется бросок?
            //String num = String.valueOf( basicRolls[ Integer.parseInt(itemstack.getIconIndex().toString()) ].dice );


        roll(itemstack, entityplayer);




        /*String statName = StatCollector.translateToLocalFormatted( localePrefix + ".name");
            int lvl = itemstack.getItemDamage();
            
            // TODO ХЗ зачем
            //String str = itemstack.getIconIndex().getIconName();
            //str = str.replaceAll("[^\\d.]", "");

            DiceRoll roll = new DiceRoll(
                    basicRolls.get(lvl),
                    entityplayer.getDisplayName(),
                    statName,
                    basicRolls.get(lvl).getModificators() // TODO: Получить дополнительные модификаторы извне. Пока сюда передаются только те модификаторы, что были включены при инициализации базовых бросков
            );
            
            //entityplayer.addChatComponentMessage(new ChatComponentText(basicRolls.get(lvl).dice + " " + statName));
            
            RollPacketToServer packet = new RollPacketToServer(roll);
            RSStats.INSTANCE.sendToServer(packet); // "123" // itemstack.getIconIndex(*/








          
        //}
        //entityplayer.addChatComponentMessage(new ChatComponentText(this.roll()));
        
        return itemstack;
        //return super.onItemRightClick(itemstack, world, entityplayer); //To change body of generated methods, choose Tools | Templates.
    }

    public void roll(ItemStack itemStack, EntityPlayer entityplayer) {
        // If нужен, чтобы броить бросок только один раз
        //if (world.isRemote) { // TODO: На какой стороне вычисляется бросок?
        //String num = String.valueOf( basicRolls[ Integer.parseInt(itemstack.getIconIndex().toString()) ].dice );
        String statName = StatCollector.translateToLocalFormatted( localePrefix + ".name");
        int lvl = itemStack.getItemDamage();

        // TODO ХЗ зачем
        //String str = itemstack.getIconIndex().getIconName();
        //str = str.replaceAll("[^\\d.]", "");

        DiceRoll roll = new DiceRoll(
                basicRolls.get(lvl),
                entityplayer.getDisplayName(),
                statName,
                basicRolls.get(lvl).getModificators() // TODO: Получить дополнительные модификаторы извне. Пока сюда передаются только те модификаторы, что были включены при инициализации базовых бросков
        );

        //entityplayer.addChatComponentMessage(new ChatComponentText(basicRolls.get(lvl).dice + " " + statName));

        RollPacketToServer packet = new RollPacketToServer(roll);
        RSStats.INSTANCE.sendToServer(packet); // "123" // itemstack.getIconIndex(

        //}
        //entityplayer.addChatComponentMessage(new ChatComponentText(this.roll()));

        //return itemstack;
        //return super.onItemRightClick(itemstack, world, entityplayer); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    /*public float getStrVsBlock(ItemStack stack, Block block, int meta) 
    {
        entityplayer.addChatMessage("Open inventory");
        
        return (float) 1.0;
    }*/
}