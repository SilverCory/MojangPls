/*
 * http://ryred.co/
 * ace[at]ac3-servers.eu
 *
 * =================================================================
 *
 * Copyright (c) 2016, Cory Redmond
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of MojangPls nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package co.ryred.mojangpls;

import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Cory Redmond on 17/05/2016.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MojangPlsMain {

	private static File dotMinecraftDir;
	private static String javaAgentProperty;
	private static JTextField dir = new JTextField();

	public static void main( String... args ) {

		try {

			String homeDir = System.getProperty( "user.home" );
			if ( System.getProperty( "os.name" ).toUpperCase().contains( "WIN" ) ) {
				homeDir = System.getenv( "AppData" );
				if ( homeDir == null || homeDir.trim().isEmpty() ) homeDir = System.getProperty( "user.home" );
			}

			dotMinecraftDir = new File( homeDir, ".minecraft" );

			System.out.println( dotMinecraftDir.getAbsolutePath() );
			if ( !dotMinecraftDir.exists() || !dotMinecraftDir.isDirectory() ) {

				final JFrame fileChooser = new JFrame( "Please select your .minecraft folder." );
				fileChooser.getContentPane().setLayout( new GridLayout( 2, 1 ) );
				fileChooser.add( new JLabel( "<html><h3>Please select your .minecraft folder...</h3></html>" ) );
				JButton okayButton = new JButton( "Okay" );
				okayButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( ActionEvent e ) {
						JFileChooser c = new JFileChooser();
						c.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
						int rVal = c.showOpenDialog( fileChooser );
						if ( rVal == JFileChooser.APPROVE_OPTION ) {
							dotMinecraftDir = c.getSelectedFile();
							fileChooser.dispose();
							doDownloadEct();
						}
						if ( rVal == JFileChooser.CANCEL_OPTION ) {
							fileChooser.dispose();
							System.exit( 0 );
						}
					}
				} );
				fileChooser.add( okayButton );
				fileChooser.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
				fileChooser.pack();
				fileChooser.setVisible( true );

			} else {
				doDownloadEct();
			}

		} catch ( Exception ex ) {
			doError( ex );
			ex.printStackTrace();
		}


	}

	private static void doDownloadEct() {
		try {

			System.out.println( dotMinecraftDir.getAbsolutePath() );
			File bypasserJar = new File( dotMinecraftDir, "blacklist_bypass.jar" );

			URL website = new URL( "http://old_checker.ryred.co/MojangBlacklistBypass.jar" );
			ReadableByteChannel rbc = Channels.newChannel( website.openStream() );
			FileOutputStream fos = new FileOutputStream( bypasserJar );
			fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );

			javaAgentProperty = "-javaagent:\"" + bypasserJar.getAbsolutePath() + "\"";

			final JFrame checkClosedFrame = new JFrame( "Is the launcher closed?!" );
			JLabel label = new JLabel( "<html><h3>Please make sure the Minecraft launcher is closed! Click okay when it is..</h3></html>" );
			JButton okayButton = new JButton( "Okay" );
			okayButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					checkClosedFrame.dispose();
					launcherIsClosed();
				}
			} );
			JButton cancelButton = new JButton( "Actually..." );
			cancelButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					checkClosedFrame.dispose();
					System.exit( 0 );
				}
			} );
			checkClosedFrame.getContentPane().setLayout( new GridLayout( 2, 2 ) );
			checkClosedFrame.add( label );
			checkClosedFrame.add( new Label() );
			checkClosedFrame.add( okayButton );
			checkClosedFrame.add( cancelButton );
			checkClosedFrame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
			checkClosedFrame.pack();
			checkClosedFrame.setVisible( true );

		} catch ( Exception ex ) {
			ex.printStackTrace();
			doError( ex );
		}
	}

	private static void doError( Exception ex ) {

		JFrame errorFrame = new JFrame( "An error occurred!" );
		errorFrame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		JLabel label = new JLabel( "<html><h3>An error occurred</h3><br/>The following error happened:<br/>\t" + ex.getMessage() + "</html>", SwingConstants.HORIZONTAL );
		errorFrame.add( label );
		errorFrame.pack();
		errorFrame.setVisible( true );

	}

	public static void launcherIsClosed() {
		try {
			File profilesFile = new File( dotMinecraftDir, "launcher_profiles.json" );

			if ( !profilesFile.exists() || !profilesFile.isFile() ) {
				JFrame errorFrame = new JFrame( "Error!" );
				errorFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				JLabel label = new JLabel( "<html><h3>There was an error finding the launcher properties file!</h3><br>Please ensure you've ran the minecraft launcher once, and you selected the correct <i>.minecraft</i> folder.</html>", SwingConstants.HORIZONTAL );
				errorFrame.getContentPane().add( label, BorderLayout.CENTER );
				errorFrame.pack();
				errorFrame.setVisible( true );
				return;
			}

			Files.copy( profilesFile.toPath(), new FileOutputStream( new File( dotMinecraftDir, "launcher_profiles_bak_" + getCurrentTimeStamp() + ".json" ) ) );

			String json = new Scanner( profilesFile ).useDelimiter( "\\A" ).next();

			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

			JsonObject root = gson.fromJson( json, JsonObject.class );
			if ( root.has( "profiles" ) ) {

				JsonObject profiles = root.getAsJsonObject( "profiles" );
				for ( Map.Entry<String, JsonElement> profile : profiles.entrySet() ) {
					JsonObject profileObject = profile.getValue().getAsJsonObject();

					if ( profileObject.has( "javaArgs" ) ) {

						String javaArgs = profileObject.getAsJsonPrimitive( "javaArgs" ).getAsString();
						if ( javaArgs.toUpperCase().contains( "-javaagent:".toUpperCase() ) ) {

							// TODO replace javaagent?

						} else {
							profileObject.add( "javaArgs", new JsonPrimitive( javaAgentProperty + " " + javaArgs ) );
						}

					} else {
						profileObject.add( "javaArgs", new JsonPrimitive( javaAgentProperty ) );
					}

				}

			}

			FileWriter fw = new FileWriter( profilesFile );
			json = gson.toJson( root );
			System.out.println( json );
			fw.write( json );
			fw.flush();
			fw.close();

			final JFrame jFrame = new JFrame( "LIBERTY!" );
			jFrame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
			jFrame.add( new JLabel( "<html><h1>LIBERTY!</h1></html>" ) );
			jFrame.pack();
			jFrame.setVisible( true );

			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep( 6000L );
						jFrame.dispose();
						System.exit( 0 );
					} catch ( InterruptedException e ) {
					}
				}
			}.start();

		} catch ( Exception ex ) {
			doError( ex );
			ex.printStackTrace();
		}
	}

	public static String getCurrentTimeStamp() {
		return new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() );
	}

}
