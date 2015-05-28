/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.plugin.PlugIn;
import mcib3d.utils.AboutMCIB;

/**
 *
 * @author thomasb
 */
public class About implements PlugIn{

    @Override
    public void run(String string) {
       AboutMCIB about = new AboutMCIB("");
        about.drawAbout();
    }
}
