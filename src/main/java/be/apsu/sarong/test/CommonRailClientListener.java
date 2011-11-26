/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.apsu.sarong.test;

import java.net.URL;
import java.util.List;

/**
 *
 * @author frank@apsu.be
 */
public interface CommonRailClientListener
{
    public void commonRailConnected(URL url);
    public void commonRailDisconnected(URL url);
    public void commonRailShuttle(List<String> lines);
}
