/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.bees;

import static madkit.bees.BeeLauncher.BEE_ROLE;
import static madkit.bees.BeeLauncher.COMMUNITY;
import static madkit.bees.BeeLauncher.LAUNCHER_ROLE;
import static madkit.bees.BeeLauncher.SCHEDULER_ROLE;
import static madkit.bees.BeeLauncher.SIMU_GROUP;
import static madkit.bees.BeeLauncher.VIEWER_ROLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import madkit.action.KernelAction;
import madkit.action.SchedulingAction;
import madkit.bees.BeeLauncher.BeeLauncherAction;
import madkit.gui.AgentFrame;
import madkit.gui.SwingUtil;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.LaunchAgentsMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.message.EnumMessage;
import madkit.message.KernelMessage;
import madkit.message.ObjectMessage;
import madkit.message.SchedulingMessage;
import madkit.simulation.probe.PropertyProbe;
import madkit.simulation.viewer.SwingViewer;

/**
 * @version 2.0.0.2
 * @author Fabien Michel, Olivier Gutknecht
 */
@SuppressWarnings("serial")
public class BeeViewer extends SwingViewer {

	private final BeeEnvironment environment;
	private JPanel display;
	private AbstractAction synchroPaint, artMode, randomMode, launch, trailModeAction, multicoreMode;
	private PropertyProbe<AbstractBee, BeeInformation> beeProbe;
	private final BeeScheduler sch;
	protected int nbOfBeesToLaunch = 30000;
	public static int nbOfBroadcast = 0;

	public BeeViewer(BeeScheduler beeScheduler) {
		sch = beeScheduler;
		environment = new BeeEnvironment(new Dimension(1600, 1024));
	}

	@Override
	protected void activate() {
		requestRole(COMMUNITY, SIMU_GROUP, "bee observer");
		beeProbe = new PropertyProbe<AbstractBee, BeeInformation>(COMMUNITY, SIMU_GROUP, BEE_ROLE, "myInformation") {

			@Override
			public void adding(AbstractBee bee) {
				super.adding(bee);
				// setting the environment of this new bee
				bee.setEnvironment(environment);
			}
		};
		addProbe(beeProbe);
	}

	@Override
	protected void end() {
		removeProbe(beeProbe);
		sendMessage(COMMUNITY, SIMU_GROUP, LAUNCHER_ROLE, new KernelMessage(KernelAction.EXIT));
		sendMessage(COMMUNITY, SIMU_GROUP, SCHEDULER_ROLE, new SchedulingMessage(SchedulingAction.SHUTDOWN));// stopping
																												// the
																												// scheduler
		leaveRole(COMMUNITY, SIMU_GROUP, VIEWER_ROLE);
	}

	@Override
	protected void render(Graphics g) {
		if (g != null) {
			computeFromInfoProbe(g);
		}
	}

	private void computeFromInfoProbe(Graphics g) {
		g.drawString("You are watching " + beeProbe.size() + " MaDKit agents", 10, 10);
		Color lastColor = null;
		final boolean trailMode = (Boolean) trailModeAction.getValue(Action.SELECTED_KEY);
		for (final AbstractBee arg0 : beeProbe.getCurrentAgentsList()) {
			final BeeInformation b = beeProbe.getPropertyValue(arg0);
			final Color c = b.getBeeColor();
			if (c != lastColor) {
				lastColor = c;
				g.setColor(lastColor);
			}
			final Point p = b.getCurrentPosition();
			if (arg0 instanceof QueenBee) {
				int size = 50;
				g.fillOval(p.x-size/2, p.y-size/2, size, size);
			} else {
				if (trailMode) {
					final Point p1 = b.getPreviousPosition();
					g.drawLine(p1.x, p1.y, p.x, p.y);
				} else {
					g.drawLine(p.x, p.y, p.x, p.y);
				}
			}
		}
	}

	@Override
	public void setupFrame(AgentFrame frame) {
		super.setupFrame(frame);
		buildActions(frame);
		frame.setBackground(Color.black);
		JMenuBar jmenubar = new JMenuBar();
		jmenubar.add(new MadkitMenu(this));
		jmenubar.add(new AgentMenu(this));
		jmenubar.add(new LaunchAgentsMenu(this));
		jmenubar.add(new AgentLogLevelMenu(this));
		jmenubar.add(sch.getSchedulerMenu());
		JMenu options = new JMenu("Options");
		options.add(new JCheckBoxMenuItem(synchroPaint));
		options.add(new JCheckBoxMenuItem(artMode));
		options.add(new JCheckBoxMenuItem(randomMode));
		options.add(new JCheckBoxMenuItem(trailModeAction));
		options.add(launch);
		jmenubar.add(options);

		ActionListener beeLaunchActionListener = evt -> sendLaunchMessage(Integer.parseInt(evt.getActionCommand()));

		JMenu numberOfBees = new JMenu("Number of bees to launch when clicking the icon");
		JMenu launchBees = new JMenu("Launching");
		ButtonGroup bgroup = new ButtonGroup();
		int defaultBeesNb = 10000;
		for (int i = 1000; i <= 1000000; i *= 10) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem("Launch " + i + " bees");
			item.setActionCommand(new Integer(i).toString().toString());
			item.addActionListener(e -> nbOfBeesToLaunch = Integer.parseInt(e.getActionCommand()));
			JMenuItem it = new JMenuItem("Launch " + i + " bees");
			it.addActionListener(beeLaunchActionListener);
			it.setActionCommand("" + i);
			launchBees.add(it);
			item.setActionCommand("" + i);
			if (i == defaultBeesNb)
				item.setSelected(true);
			bgroup.add(item);
			numberOfBees.add(item);
		}
		options.add(numberOfBees);
		jmenubar.add(launchBees);

		frame.setJMenuBar(jmenubar);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		display = new JPanel() {

			@Override
			protected void paintComponent(Graphics g) {
				if (!(Boolean) artMode.getValue(Action.SELECTED_KEY)) {
					super.paintComponent(g);
				}
				render(g);
			}
		};
		setDisplayPane(display);
		display.setBackground(Color.BLACK);
		display.setForeground(Color.white);
		frame.add(display);
		display.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				environment.setEnvSize(e.getComponent().getSize());
				if (beeProbe != null) {
					beeProbe.initialize();
				}
			}
		});
		JToolBar tb = new JToolBar();
		addButtonToToolbar(tb, randomMode);
		addButtonToToolbar(tb, artMode);
		addButtonToToolbar(tb, trailModeAction);
		addButtonToToolbar(tb, synchroPaint);
		addButtonToToolbar(tb, launch);
		addButtonToToolbar(tb, multicoreMode);

		JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tools.add(tb);

		tools.add(sch.getSchedulerToolBar());

		JToolBar modelProperties = new JToolBar();
		modelProperties.add(SwingUtil.createSliderPanel(environment.getQueenAcceleration(), "queen acceleration"));
		modelProperties.add(SwingUtil.createSliderPanel(environment.getQueenVelocity(), "queen velocity"));
		modelProperties.add(SwingUtil.createSliderPanel(environment.getBeeAcceleration(), "bee acceleration"));
		modelProperties.add(SwingUtil.createSliderPanel(environment.getBeeVelocity(), "bee velocity"));
		tools.add(modelProperties);

		frame.add(sch.getSchedulerStatusLabel(), BorderLayout.SOUTH);
		display.getParent().add(tools, BorderLayout.PAGE_START);
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * @param tb
	 */
	private void addButtonToToolbar(JToolBar tb, Action a) {
		JToggleButton jt = new JToggleButton(a);
		jt.setText(null);
		tb.add(jt);
	}

	void buildActions(final JFrame frame) {
		synchroPaint = new AbstractAction("Synchronous painting") {

			@Override
			public void actionPerformed(ActionEvent e) {
				setSynchronousPainting(!(Boolean) synchroPaint.getValue(Action.SELECTED_KEY));
			}
		};
		initActionIcon(synchroPaint, "Deactivate the synchronous painting mode (faster)", "synchroPaint");
		synchroPaint.putValue(Action.SELECTED_KEY, false);
		artMode = new AbstractAction("Art mode") {

			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
		initActionIcon(artMode, "A funny painting mode", "artMode");

		randomMode = new AbstractAction("Random mode") {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(COMMUNITY, SIMU_GROUP, LAUNCHER_ROLE,
						new EnumMessage<>(BeeLauncherAction.RANDOM_MODE, randomMode.getValue(SELECTED_KEY)));
			}
		};
		initActionIcon(randomMode, "Random mode: Randomly launch or kill bees", "random");
		randomMode.putValue(Action.SELECTED_KEY, true);

		multicoreMode = new AbstractAction("Multicore mode") {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(COMMUNITY, SIMU_GROUP, BeeLauncher.SCHEDULER_ROLE,
						new ObjectMessage<>((Boolean) multicoreMode.getValue(SELECTED_KEY)));
			}
		};
		initActionIcon(multicoreMode,
				"Multicore mode: Use several processor cores if available (more efficient if synchro painting is off",
				"multicore");
		// randomMode.putValue(Action.SELECTED_KEY, false);

		trailModeAction = new AbstractAction("Trail mode") {

			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
		initActionIcon(trailModeAction, "Trails mode: display agents with trails or like point particles", "trail");
		trailModeAction.putValue(Action.SELECTED_KEY, true);

		launch = new AbstractAction("Launch bees") {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendLaunchMessage(nbOfBeesToLaunch);
			}
		};
		initActionIcon(launch, "Launch some bees", "launch");
	}

	private void initActionIcon(AbstractAction a, String description, String actionCommand) {
		a.putValue(Action.SELECTED_KEY, false);
		a.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		a.putValue(AbstractAction.SHORT_DESCRIPTION, description);
		ImageIcon big = new ImageIcon(getClass().getResource("images/bees_" + actionCommand + ".png"));
		a.putValue(AbstractAction.LARGE_ICON_KEY, big);
		a.putValue(AbstractAction.SMALL_ICON,
				new ImageIcon(big.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
	}

	private void sendLaunchMessage(int nb) {
		sendMessage(COMMUNITY, SIMU_GROUP, LAUNCHER_ROLE, new EnumMessage<>(BeeLauncherAction.LAUNCH_BEES, nb));

	}

}