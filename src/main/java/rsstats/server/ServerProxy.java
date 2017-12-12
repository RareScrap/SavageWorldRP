/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.server;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import rsstats.common.CommonProxy;
import rsstats.utils.DiceRoll;

import java.util.ArrayList;

/**
 *
 * @author rares
 */
public class ServerProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e, ArrayList<DiceRoll> dices) {
        super.preInit(e, dices);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
}
