/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.data;

/**
 *
 * @author rares
 */
public class StatUnit {
    
    public String name;
    public int value;
    
    public StatUnit(String Name, int Value) {
        name = Name;
        value = Value;
        //Тут нужно сделать загрузку из даты игрока (короч тут присваивается значения статам)
    }
}
