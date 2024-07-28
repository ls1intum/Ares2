package de.tum.cit.ase.ares.api.aspectconfiguration.java;

public aspect PointcutDefinitions {

        // Pointcut for Applet constructors and methods
        pointcut appletMethods() :
                (execution(java.applet.Applet.new(..)) ||
                        execution(* java.applet.Applet.getAudioClip(..)) ||
                        execution(* java.applet.Applet.getImage(..)) ||
                        execution(* java.applet.Applet.play(..)) ||
                        execution(* java.applet.Applet.resize(..)) ||
                        execution(* java.applet.Applet.setStub(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AWTEvent constructors and methods
        pointcut awtEventMethods() :
                (execution(java.awt.AWTEvent.new(..)) ||
                        execution(* java.awt.AWTEvent.toString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AWTException constructors
        pointcut awtExceptionMethods() :
                (execution(java.awt.AWTException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AWTPermission constructors
        pointcut awtPermissionMethods() :
                (execution(java.awt.AWTPermission.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BasicStroke methods
        pointcut basicStrokeMethods() :
                (execution(* java.awt.BasicStroke.createStrokedShape(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BorderLayout methods
        pointcut borderLayoutMethods() :
                (execution(* java.awt.BorderLayout.layoutContainer(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Button constructors and methods
        pointcut buttonMethods() :
                (execution(java.awt.Button.new(..)) ||
                        execution(* java.awt.Button.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Canvas constructors and methods
        pointcut canvasMethods() :
                (execution(java.awt.Canvas.new(..)) ||
                        execution(* java.awt.Canvas.addNotify(..)) ||
                        execution(* java.awt.Canvas.createBufferStrategy(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CardLayout methods
        pointcut cardLayoutMethods() :
                (execution(* java.awt.CardLayout.addLayoutComponent(..)) ||
                        execution(* java.awt.CardLayout.first(..)) ||
                        execution(* java.awt.CardLayout.last(..)) ||
                        execution(* java.awt.CardLayout.layoutContainer(..)) ||
                        execution(* java.awt.CardLayout.next(..)) ||
                        execution(* java.awt.CardLayout.previous(..)) ||
                        execution(* java.awt.CardLayout.removeLayoutComponent(..)) ||
                        execution(* java.awt.CardLayout.show(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Checkbox constructors and methods
        pointcut checkboxMethods() :
                (execution(java.awt.Checkbox.new(..)) ||
                        execution(* java.awt.Checkbox.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CheckboxMenuItem constructors and methods
        pointcut checkboxMenuItemMethods() :
                (execution(java.awt.CheckboxMenuItem.new(..)) ||
                        execution(* java.awt.CheckboxMenuItem.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Choice constructors and methods
        pointcut choiceMethods() :
                (execution(java.awt.Choice.new(..)) ||
                        execution(* java.awt.Choice.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Component read methods
        pointcut componentReadMethods() :
                (execution(* java.awt.Component.getColorModel(..)) ||
                        execution(* java.awt.Component.getCursor(..)) ||
                        execution(* java.awt.Component.getFocusTraversalKeys(..)) ||
                        execution(* java.awt.Component.getFontMetrics(..)) ||
                        execution(* java.awt.Component.getMousePosition(..)) ||
                        execution(* java.awt.Component.getToolkit(..)) ||
                        execution(* java.awt.Component.hasFocus(..)) ||
                        execution(* java.awt.Component.isFocusOwner(..)) ||
                        execution(* java.awt.Component.list(..)) ||
                        execution(* java.awt.Component.imageUpdate(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Component write methods
        pointcut componentWriteMethods() :
                (execution(* java.awt.Component.add(..)) ||
                        execution(* java.awt.Component.addNotify(..)) ||
                        execution(* java.awt.Component.applyComponentOrientation(..)) ||
                        execution(* java.awt.Component.checkImage(..)) ||
                        execution(* java.awt.Component.createImage(..)) ||
                        execution(* java.awt.Component.enable(..)) ||
                        execution(* java.awt.Component.enableInputMethods(..)) ||
                        execution(* java.awt.Component.firePropertyChange(..)) ||
                        execution(* java.awt.Component.move(..)) ||
                        execution(* java.awt.Component.prepareImage(..)) ||
                        execution(* java.awt.Component.remove(..)) ||
                        execution(* java.awt.Component.removeNotify(..)) ||
                        execution(* java.awt.Component.repaint(..)) ||
                        execution(* java.awt.Component.requestFocus(..)) ||
                        execution(* java.awt.Component.reshape(..)) ||
                        execution(* java.awt.Component.resize(..)) ||
                        execution(* java.awt.Component.revalidate(..)) ||
                        execution(* java.awt.Component.setBackground(..)) ||
                        execution(* java.awt.Component.setBounds(..)) ||
                        execution(* java.awt.Component.setComponentOrientation(..)) ||
                        execution(* java.awt.Component.setEnabled(..)) ||
                        execution(* java.awt.Component.setFocusTraversalKeys(..)) ||
                        execution(* java.awt.Component.setFocusTraversalKeysEnabled(..)) ||
                        execution(* java.awt.Component.setFocusable(..)) ||
                        execution(* java.awt.Component.setFont(..)) ||
                        execution(* java.awt.Component.setForeground(..)) ||
                        execution(* java.awt.Component.setLocale(..)) ||
                        execution(* java.awt.Component.setLocation(..)) ||
                        execution(* java.awt.Component.setMaximumSize(..)) ||
                        execution(* java.awt.Component.setMinimumSize(..)) ||
                        execution(* java.awt.Component.setName(..)) ||
                        execution(* java.awt.Component.setPreferredSize(..)) ||
                        execution(* java.awt.Component.setSize(..)) ||
                        execution(* java.awt.Component.setVisible(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Component execute methods
        pointcut componentExecuteMethods() :
                (execution(* java.awt.Component.disable(..)) ||
                        execution(* java.awt.Component.dispatchEvent(..)) ||
                        execution(* java.awt.Component.enable(..)) ||
                        execution(* java.awt.Component.hide(..)) ||
                        execution(* java.awt.Component.show(..)) ||
                        execution(* java.awt.Component.transferFocus(..)) ||
                        execution(* java.awt.Component.transferFocusBackward(..)) ||
                        execution(* java.awt.Component.transferFocusUpCycle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ComponentOrientation methods
        pointcut componentOrientationMethods() :
                (execution(* java.awt.ComponentOrientation.getOrientation(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Container constructors and read methods
        pointcut containerReadMethods() :
                (execution(java.awt.Container.new(..)) ||
                        execution(* java.awt.Container.getFocusTraversalKeys(..)) ||
                        execution(* java.awt.Container.getFocusTraversalPolicy(..)) ||
                        execution(* java.awt.Container.getMousePosition(..)) ||
                        execution(* java.awt.Container.list(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Container write methods
        pointcut containerWriteMethods() :
                (execution(* java.awt.Container.add(..)) ||
                        execution(* java.awt.Container.addNotify(..)) ||
                        execution(* java.awt.Container.applyComponentOrientation(..)) ||
                        execution(* java.awt.Container.doLayout(..)) ||
                        execution(* java.awt.Container.remove(..)) ||
                        execution(* java.awt.Container.removeAll(..)) ||
                        execution(* java.awt.Container.removeNotify(..)) ||
                        execution(* java.awt.Container.setComponentZOrder(..)) ||
                        execution(* java.awt.Container.setFocusCycleRoot(..)) ||
                        execution(* java.awt.Container.setFocusTraversalKeys(..)) ||
                        execution(* java.awt.Container.setFocusTraversalPolicy(..)) ||
                        execution(* java.awt.Container.setFocusTraversalPolicyProvider(..)) ||
                        execution(* java.awt.Container.setFont(..)) ||
                        execution(* java.awt.Container.update(..)) ||
                        execution(* java.awt.Container.validate(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Container execute methods
        pointcut containerExecuteMethods() :
                (execution(* java.awt.Container.transferFocusDownCycle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ContainerOrderFocusTraversalPolicy methods
        pointcut containerOrderFocusTraversalPolicyMethods() :
                (execution(* java.awt.ContainerOrderFocusTraversalPolicy.getComponentAfter(..)) ||
                        execution(* java.awt.ContainerOrderFocusTraversalPolicy.getComponentBefore(..)) ||
                        execution(* java.awt.ContainerOrderFocusTraversalPolicy.getDefaultComponent(..)) ||
                        execution(* java.awt.ContainerOrderFocusTraversalPolicy.getFirstComponent(..)) ||
                        execution(* java.awt.ContainerOrderFocusTraversalPolicy.getLastComponent(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Cursor constructors and methods
        pointcut cursorMethods() :
                (execution(java.awt.Cursor.new(..)) ||
                        execution(* java.awt.Cursor.getDefaultCursor(..)) ||
                        execution(* java.awt.Cursor.getPredefinedCursor(..)) ||
                        execution(* java.awt.Cursor.getSystemCustomCursor(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DefaultKeyboardFocusManager constructors and methods
        pointcut defaultKeyboardFocusManagerMethods() :
                (execution(java.awt.DefaultKeyboardFocusManager.new(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.dispatchEvent(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.dispatchKeyEvent(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.downFocusCycle(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.focusNextComponent(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.focusPreviousComponent(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.processKeyEvent(..)) ||
                        execution(* java.awt.DefaultKeyboardFocusManager.upFocusCycle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Desktop read methods
        pointcut desktopReadMethods() :
                (execution(* java.awt.Desktop.getDesktop(..)) ||
                        execution(* java.awt.Desktop.isDesktopSupported(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Desktop write methods
        pointcut desktopWriteMethods() :
                (execution(* java.awt.Desktop.setDefaultMenuBar(..)) ||
                        execution(* java.awt.Desktop.setOpenFileHandler(..)) ||
                        execution(* java.awt.Desktop.setOpenURIHandler(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Desktop execute methods
        pointcut desktopExecuteMethods() :
                (execution(* java.awt.Desktop.browse(..)) ||
                        execution(* java.awt.Desktop.browseFileDirectory(..)) ||
                        execution(* java.awt.Desktop.edit(..)) ||
                        execution(* java.awt.Desktop.mail(..)) ||
                        execution(* java.awt.Desktop.moveToTrash(..)) ||
                        execution(* java.awt.Desktop.open(..)) ||
                        execution(* java.awt.Desktop.openHelpViewer(..)) ||
                        execution(* java.awt.Desktop.print(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Dialog constructors and methods
        pointcut dialogMethods() :
                (execution(java.awt.Dialog.new(..)) ||
                        execution(* java.awt.Dialog.addNotify(..)) ||
                        execution(* java.awt.Dialog.hide(..)) ||
                        execution(* java.awt.Dialog.setBackground(..)) ||
                        execution(* java.awt.Dialog.setModal(..)) ||
                        execution(* java.awt.Dialog.setModalityType(..)) ||
                        execution(* java.awt.Dialog.setOpacity(..)) ||
                        execution(* java.awt.Dialog.setShape(..)) ||
                        execution(* java.awt.Dialog.setTitle(..)) ||
                        execution(* java.awt.Dialog.setVisible(..)) ||
                        execution(* java.awt.Dialog.show(..)) ||
                        execution(* java.awt.Dialog.toBack(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for EventQueue methods
        pointcut eventQueueMethods() :
                (execution(* java.awt.EventQueue.createSecondaryLoop(..)) ||
                        execution(* java.awt.EventQueue.getCurrentEvent(..)) ||
                        execution(* java.awt.EventQueue.getMostRecentEventTime(..)) ||
                        execution(* java.awt.EventQueue.getNextEvent(..)) ||
                        execution(* java.awt.EventQueue.invokeAndWait(..)) ||
                        execution(* java.awt.EventQueue.invokeLater(..)) ||
                        execution(* java.awt.EventQueue.isDispatchThread(..)) ||
                        execution(* java.awt.EventQueue.postEvent(..)) ||
                        execution(* java.awt.EventQueue.push(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileDialog constructors and methods
        pointcut fileDialogConstructorMethods() :
                (execution(java.awt.FileDialog.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut fileDialogWriteMethods() :
                (execution(* java.awt.FileDialog.setTitle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut fileDialogExecuteMethods() :
                (execution(* java.awt.FileDialog.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Font constructors and methods
        pointcut fontConstructorMethods() :
                (execution(java.awt.Font.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut fontReadMethods() :
                (execution(* java.awt.Font.canDisplay(..)) ||
                        execution(* java.awt.Font.canDisplayUpTo(..)) ||
                        execution(* java.awt.Font.createFont(..)) ||
                        execution(* java.awt.Font.createFonts(..)) ||
                        execution(* java.awt.Font.createGlyphVector(..)) ||
                        execution(* java.awt.Font.deriveFont(..)) ||
                        execution(* java.awt.Font.getAttributes(..)) ||
                        execution(* java.awt.Font.getFont(..)) ||
                        execution(* java.awt.Font.getItalicAngle(..)) ||
                        execution(* java.awt.Font.getLineMetrics(..)) ||
                        execution(* java.awt.Font.getMaxCharBounds(..)) ||
                        execution(* java.awt.Font.getMissingGlyphCode(..)) ||
                        execution(* java.awt.Font.getNumGlyphs(..)) ||
                        execution(* java.awt.Font.getStringBounds(..)) ||
                        execution(* java.awt.Font.getTransform(..)) ||
                        execution(* java.awt.Font.layoutGlyphVector(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut fontWriteMethods() :
                (execution(* java.awt.Font.equals(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FlowLayout methods
        pointcut flowLayoutMethods() :
                (execution(* java.awt.FlowLayout.layoutContainer(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FocusTraversalPolicy methods
        pointcut focusTraversalPolicyMethods() :
                (execution(* java.awt.FocusTraversalPolicy.getInitialComponent(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FontMetrics read methods
        pointcut fontMetricsReadMethods() :
                (execution(* java.awt.FontMetrics.bytesWidth(..)) ||
                        execution(* java.awt.FontMetrics.charWidth(..)) ||
                        execution(* java.awt.FontMetrics.charsWidth(..)) ||
                        execution(* java.awt.FontMetrics.getLineMetrics(..)) ||
                        execution(* java.awt.FontMetrics.getMaxCharBounds(..)) ||
                        execution(* java.awt.FontMetrics.getStringBounds(..)) ||
                        execution(* java.awt.FontMetrics.getWidths(..)) ||
                        execution(* java.awt.FontMetrics.stringWidth(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Frame constructors
        pointcut frameConstructorMethods() :
                (execution(java.awt.Frame.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Frame methods
        pointcut frameWriteMethods() :
                (execution(* java.awt.Frame.addNotify(..)) ||
                        execution(* java.awt.Frame.setBackground(..)) ||
                        execution(* java.awt.Frame.setCursor(..)) ||
                        execution(* java.awt.Frame.setExtendedState(..)) ||
                        execution(* java.awt.Frame.setIconImage(..)) ||
                        execution(* java.awt.Frame.setMenuBar(..)) ||
                        execution(* java.awt.Frame.setOpacity(..)) ||
                        execution(* java.awt.Frame.setResizable(..)) ||
                        execution(* java.awt.Frame.setShape(..)) ||
                        execution(* java.awt.Frame.setState(..)) ||
                        execution(* java.awt.Frame.setTitle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut frameExecuteMethods() :
                (execution(* java.awt.Frame.getCursorType(..)) ||
                        execution(* java.awt.Frame.remove(..)) ||
                        execution(* java.awt.Frame.removeNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GradientPaint create context method
        pointcut gradientPaintMethods() :
                (execution(* java.awt.GradientPaint.createContext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GraphicsConfiguration create methods
        pointcut graphicsConfigurationWriteMethods() :
                (execution(* java.awt.GraphicsConfiguration.createCompatibleVolatileImage(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GraphicsDevice methods
        pointcut graphicsDeviceReadMethods() :
                (execution(* java.awt.GraphicsDevice.getBestConfiguration(..)) ||
                        execution(* java.awt.GraphicsDevice.getDisplayMode(..)) ||
                        execution(* java.awt.GraphicsDevice.getDisplayModes(..)) ||
                        execution(* java.awt.GraphicsDevice.isWindowTranslucencySupported(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut graphicsDeviceWriteMethods() :
                (execution(* java.awt.GraphicsDevice.setFullScreenWindow(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GraphicsEnvironment methods
        pointcut graphicsEnvironmentReadMethods() :
                (execution(* java.awt.GraphicsEnvironment.getCenterPoint(..)) ||
                        execution(* java.awt.GraphicsEnvironment.getMaximumWindowBounds(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GridBagLayout and GridLayout methods
        pointcut layoutContainerWriteMethods() :
                (execution(* java.awt.GridBagLayout.layoutContainer(..)) ||
                        execution(* java.awt.GridLayout.layoutContainer(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Image methods
        pointcut imageWriteMethods() :
                (execution(* java.awt.Image.flush(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut imageReadMethods() :
                (execution(* java.awt.Image.getScaledInstance(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for KeyboardFocusManager methods
        pointcut keyboardFocusManagerConstructorMethods() :
                (execution(java.awt.KeyboardFocusManager.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut keyboardFocusManagerWriteMethods() :
                (execution(* java.awt.KeyboardFocusManager.clearFocusOwner(..)) ||
                        execution(* java.awt.KeyboardFocusManager.clearGlobalFocusOwner(..)) ||
                        execution(* java.awt.KeyboardFocusManager.downFocusCycle(..)) ||
                        execution(* java.awt.KeyboardFocusManager.focusNextComponent(..)) ||
                        execution(* java.awt.KeyboardFocusManager.focusPreviousComponent(..)) ||
                        execution(* java.awt.KeyboardFocusManager.setCurrentKeyboardFocusManager(..)) ||
                        execution(* java.awt.KeyboardFocusManager.setDefaultFocusTraversalKeys(..)) ||
                        execution(* java.awt.KeyboardFocusManager.setDefaultFocusTraversalPolicy(..)) ||
                        execution(* java.awt.KeyboardFocusManager.setGlobalCurrentFocusCycleRoot(..)) ||
                        execution(* java.awt.KeyboardFocusManager.upFocusCycle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut keyboardFocusManagerExecuteMethods() :
                (execution(* java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager(..)) ||
                        execution(* java.awt.KeyboardFocusManager.redispatchEvent(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Label constructor and addNotify method
        pointcut labelConstructorMethods() :
                (execution(java.awt.Label.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut labelAddNotifyMethod() :
                (execution(* java.awt.Label.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LinearGradientPaint createContext method
        pointcut linearGradientPaintCreateContextMethod() :
                (execution(* java.awt.LinearGradientPaint.createContext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for List constructor and addNotify method
        pointcut listConstructorMethods() :
                (execution(java.awt.List.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut listAddNotifyMethod() :
                (execution(* java.awt.List.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MediaTracker check methods
        pointcut mediaTrackerReadMethods() :
                (execution(* java.awt.MediaTracker.checkAll(..)) ||
                        execution(* java.awt.MediaTracker.checkID(..)) ||
                        execution(* java.awt.MediaTracker.getErrorsAny(..)) ||
                        execution(* java.awt.MediaTracker.getErrorsID(..)) ||
                        execution(* java.awt.MediaTracker.isErrorAny(..)) ||
                        execution(* java.awt.MediaTracker.isErrorID(..)) ||
                        execution(* java.awt.MediaTracker.statusAll(..)) ||
                        execution(* java.awt.MediaTracker.statusID(..)) ||
                        execution(* java.awt.MediaTracker.waitForAll(..)) ||
                        execution(* java.awt.MediaTracker.waitForID(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Menu constructor and methods
        pointcut menuConstructorMethods() :
                (execution(java.awt.Menu.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut menuWriteMethods() :
                (execution(* java.awt.Menu.add(..)) ||
                        execution(* java.awt.Menu.addNotify(..)) ||
                        execution(* java.awt.Menu.addSeparator(..)) ||
                        execution(* java.awt.Menu.insert(..)) ||
                        execution(* java.awt.Menu.remove(..)) ||
                        execution(* java.awt.Menu.removeAll(..)) ||
                        execution(* java.awt.Menu.removeNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MenuBar constructor and methods
        pointcut menuBarConstructorMethods() :
                (execution(java.awt.MenuBar.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut menuBarWriteMethods() :
                (execution(* java.awt.MenuBar.add(..)) ||
                        execution(* java.awt.MenuBar.addNotify(..)) ||
                        execution(* java.awt.MenuBar.remove(..)) ||
                        execution(* java.awt.MenuBar.setHelpMenu(..)) ||
                        execution(* java.awt.MenuBar.removeNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MenuComponent constructor and methods
        pointcut menuComponentConstructorMethods() :
                (execution(java.awt.MenuComponent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut menuComponentWriteMethods() :
                (execution(* java.awt.MenuComponent.dispatchEvent(..)) ||
                        execution(* java.awt.MenuComponent.removeNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MenuItem constructor and methods
        pointcut menuItemConstructorMethods() :
                (execution(java.awt.MenuItem.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut menuItemWriteMethods() :
                (execution(* java.awt.MenuItem.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MouseInfo methods
        pointcut mouseInfoReadMethods() :
                (execution(* java.awt.MouseInfo.getNumberOfButtons(..)) ||
                        execution(* java.awt.MouseInfo.getPointerInfo(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Panel constructor and addNotify method
        pointcut panelConstructorMethods() :
                (execution(java.awt.Panel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut panelAddNotifyMethod() :
                (execution(* java.awt.Panel.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PopupMenu constructor and methods
        pointcut popupMenuConstructorMethods() :
                (execution(java.awt.PopupMenu.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut popupMenuWriteMethods() :
                (execution(* java.awt.PopupMenu.addNotify(..)) ||
                        execution(* java.awt.PopupMenu.show(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RadialGradientPaint createContext method
        pointcut radialGradientPaintCreateContextMethod() :
                (execution(* java.awt.RadialGradientPaint.createContext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RenderingHints clone method
        pointcut renderingHintsCloneMethod() :
                (execution(* java.awt.RenderingHints.clone(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Robot constructor and methods
        pointcut robotConstructorMethods() :
                (execution(java.awt.Robot.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut robotWriteMethods() :
                (execution(* java.awt.Robot.createMultiResolutionScreenCapture(..)) ||
                        execution(* java.awt.Robot.createScreenCapture(..)) ||
                        execution(* java.awt.Robot.getPixelColor(..)) ||
                        execution(* java.awt.Robot.keyPress(..)) ||
                        execution(* java.awt.Robot.keyRelease(..)) ||
                        execution(* java.awt.Robot.mouseMove(..)) ||
                        execution(* java.awt.Robot.mousePress(..)) ||
                        execution(* java.awt.Robot.mouseRelease(..)) ||
                        execution(* java.awt.Robot.mouseWheel(..)) ||
                        execution(* java.awt.Robot.waitForIdle(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ScrollPane constructors and methods
        pointcut scrollPaneConstructorMethods() :
                (execution(java.awt.ScrollPane.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut scrollPaneWriteMethods() :
                (execution(* java.awt.ScrollPane.addNotify(..)) ||
                        execution(* java.awt.ScrollPane.doLayout(..)) ||
                        execution(* java.awt.ScrollPane.layout(..)) ||
                        execution(* java.awt.ScrollPane.setScrollPosition(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ScrollPaneAdjustable methods
        pointcut scrollPaneAdjustableWriteMethods() :
                (execution(* java.awt.ScrollPaneAdjustable.setValue(..)) ||
                        execution(* java.awt.ScrollPaneAdjustable.setValueIsAdjusting(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Scrollbar constructors and methods
        pointcut scrollbarConstructorMethods() :
                (execution(java.awt.Scrollbar.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut scrollbarWriteMethods() :
                (execution(* java.awt.Scrollbar.addNotify(..)) ||
                        execution(* java.awt.Scrollbar.setMaximum(..)) ||
                        execution(* java.awt.Scrollbar.setMinimum(..)) ||
                        execution(* java.awt.Scrollbar.setOrientation(..)) ||
                        execution(* java.awt.Scrollbar.setValue(..)) ||
                        execution(* java.awt.Scrollbar.setValueIsAdjusting(..)) ||
                        execution(* java.awt.Scrollbar.setValues(..)) ||
                        execution(* java.awt.Scrollbar.setVisibleAmount(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SplashScreen methods
        pointcut splashScreenWriteMethods() :
                (execution(* java.awt.SplashScreen.createGraphics(..)) ||
                        execution(* java.awt.SplashScreen.setImageURL(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut splashScreenReadMethods() :
                (execution(* java.awt.SplashScreen.getImageURL(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SystemTray methods
        pointcut systemTrayWriteMethods() :
                (execution(* java.awt.SystemTray.add(..)) ||
                        execution(* java.awt.SystemTray.remove(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut systemTrayReadMethods() :
                (execution(* java.awt.SystemTray.getSystemTray(..)) ||
                        execution(* java.awt.SystemTray.isSupported(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Taskbar methods
        pointcut taskbarReadMethods() :
                (execution(* java.awt.Taskbar.getTaskbar(..)) ||
                        execution(* java.awt.Taskbar.isTaskbarSupported(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextArea constructors and addNotify method
        pointcut textAreaConstructorMethods() :
                (execution(java.awt.TextArea.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut textAreaAddNotifyMethod() :
                (execution(* java.awt.TextArea.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextComponent methods
        pointcut textComponentWriteMethods() :
                (execution(* java.awt.TextComponent.addNotify(..)) ||
                        execution(* java.awt.TextComponent.enableInputMethods(..)) ||
                        execution(* java.awt.TextComponent.removeNotify(..)) ||
                        execution(* java.awt.TextComponent.setBackground(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextField constructors and methods
        pointcut textFieldConstructorMethods() :
                (execution(java.awt.TextField.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut textFieldAddNotifyMethod() :
                (execution(* java.awt.TextField.addNotify(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TexturePaint methods
        pointcut texturePaintWriteMethods() :
                (execution(* java.awt.TexturePaint.createContext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Toolkit methods
        pointcut toolkitWriteMethods() :
                (execution(* java.awt.Toolkit.addAWTEventListener(..)) ||
                        execution(* java.awt.Toolkit.createCustomCursor(..)) ||
                        execution(* java.awt.Toolkit.removeAWTEventListener(..)) ||
                        execution(* java.awt.Toolkit.setDynamicLayout(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut toolkitReadMethods() :
                (execution(* java.awt.Toolkit.areExtraMouseButtonsEnabled(..)) ||
                        execution(* java.awt.Toolkit.getBestCursorSize(..)) ||
                        execution(* java.awt.Toolkit.getDefaultToolkit(..)) ||
                        execution(* java.awt.Toolkit.getDesktopProperty(..)) ||
                        execution(* java.awt.Toolkit.getMaximumCursorColors(..)) ||
                        execution(* java.awt.Toolkit.getPrintJob(..)) ||
                        execution(* java.awt.Toolkit.getProperty(..)) ||
                        execution(* java.awt.Toolkit.getScreenInsets(..)) ||
                        execution(* java.awt.Toolkit.getSystemSelection(..)) ||
                        execution(* java.awt.Toolkit.isDynamicLayoutActive(..)) ||
                        execution(* java.awt.Toolkit.isFrameStateSupported(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TrayIcon constructors and methods
        pointcut trayIconConstructorMethods() :
                (execution(java.awt.TrayIcon.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut trayIconReadMethods() :
                (execution(* java.awt.TrayIcon.getSize(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Window constructors and methods related to creation and manipulation
        pointcut windowConstructorMethods() :
                (execution(java.awt.Window.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut windowWriteMethods() :
                (execution(* java.awt.Window.addNotify(..)) ||
                        execution(* java.awt.Window.applyResourceBundle(..)) ||
                        execution(* java.awt.Window.createBufferStrategy(..)) ||
                        execution(* java.awt.Window.dispose(..)) ||
                        execution(* java.awt.Window.hide(..)) ||
                        execution(* java.awt.Window.pack(..)) ||
                        execution(* java.awt.Window.paint(..)) ||
                        execution(* java.awt.Window.removeNotify(..)) ||
                        execution(* java.awt.Window.reshape(..)) ||
                        execution(* java.awt.Window.setAlwaysOnTop(..)) ||
                        execution(* java.awt.Window.setBackground(..)) ||
                        execution(* java.awt.Window.setBounds(..)) ||
                        execution(* java.awt.Window.setCursor(..)) ||
                        execution(* java.awt.Window.setFocusableWindowState(..)) ||
                        execution(* java.awt.Window.setIconImage(..)) ||
                        execution(* java.awt.Window.setIconImages(..)) ||
                        execution(* java.awt.Window.setLocation(..)) ||
                        execution(* java.awt.Window.setLocationRelativeTo(..)) ||
                        execution(* java.awt.Window.setMinimumSize(..)) ||
                        execution(* java.awt.Window.setModalExclusionType(..)) ||
                        execution(* java.awt.Window.setOpacity(..)) ||
                        execution(* java.awt.Window.setShape(..)) ||
                        execution(* java.awt.Window.setSize(..)) ||
                        execution(* java.awt.Window.setVisible(..)) ||
                        execution(* java.awt.Window.show(..)) ||
                        execution(* java.awt.Window.toBack(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        pointcut windowReadMethods() :
                (execution(* java.awt.Window.getFocusOwner(..)) ||
                        execution(* java.awt.Window.getFocusTraversalKeys(..)) ||
                        execution(* java.awt.Window.getInputContext(..)) ||
                        execution(* java.awt.Window.getMostRecentFocusOwner(..)) ||
                        execution(* java.awt.Window.getToolkit(..)) ||
                        execution(* java.awt.Window.isActive(..)) ||
                        execution(* java.awt.Window.isAlwaysOnTopSupported(..)) ||
                        execution(* java.awt.Window.isFocusableWindow(..)) ||
                        execution(* java.awt.Window.isFocused(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ICC_Profile methods related to reading data
        pointcut iccProfileReadMethods() :
                (execution(* java.awt.color.ICC_Profile.getColorSpaceType(..)) ||
                        execution(* java.awt.color.ICC_Profile.getData(..)) ||
                        execution(* java.awt.color.ICC_Profile.getInstance(..)) ||
                        execution(* java.awt.color.ICC_Profile.getMajorVersion(..)) ||
                        execution(* java.awt.color.ICC_Profile.getMinorVersion(..)) ||
                        execution(* java.awt.color.ICC_Profile.getNumComponents(..)) ||
                        execution(* java.awt.color.ICC_Profile.getPCSType(..)) ||
                        execution(* java.awt.color.ICC_Profile.getProfileClass(..)) ||
                        execution(* java.awt.color.ICC_ProfileGray.getGamma(..)) ||
                        execution(* java.awt.color.ICC_ProfileGray.getMediaWhitePoint(..)) ||
                        execution(* java.awt.color.ICC_ProfileGray.getTRC(..)) ||
                        execution(* java.awt.color.ICC_ProfileRGB.getGamma(..)) ||
                        execution(* java.awt.color.ICC_ProfileRGB.getMatrix(..)) ||
                        execution(* java.awt.color.ICC_ProfileRGB.getMediaWhitePoint(..)) ||
                        execution(* java.awt.color.ICC_ProfileRGB.getTRC(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ICC_Profile methods related to writing data
        pointcut iccProfileWriteMethods() :
                (execution(* java.awt.color.ICC_Profile.setData(..)) ||
                        execution(* java.awt.color.ICC_Profile.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Clipboard methods related to adding and setting data (write operations)
        pointcut clipboardWriteMethods() :
                (execution(* java.awt.datatransfer.Clipboard.addFlavorListener(..)) ||
                        execution(* java.awt.datatransfer.Clipboard.setContents(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Clipboard methods related to getting data (read operations)
        pointcut clipboardReadMethods() :
                (execution(* java.awt.datatransfer.Clipboard.getAvailableDataFlavors(..)) ||
                        execution(* java.awt.datatransfer.Clipboard.getData(..)) ||
                        execution(* java.awt.datatransfer.Clipboard.isDataFlavorAvailable(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DataFlavor constructors and methods
        pointcut dataFlavorMethods() :
                (execution(java.awt.datatransfer.DataFlavor.new(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.equals(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.getReaderForText(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.getTextPlainUnicodeFlavor(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.isFlavorRemoteObjectType(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.isFlavorSerializedObjectType(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.isMimeTypeEqual(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.isMimeTypeSerializedObject(..)) ||
                        execution(* java.awt.datatransfer.DataFlavor.readExternal(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MimeTypeParseException constructors
        pointcut mimeTypeParseExceptionMethods() :
                (execution(java.awt.datatransfer.MimeTypeParseException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for StringSelection methods
        pointcut stringSelectionMethods() :
                (execution(* java.awt.datatransfer.StringSelection.getTransferData(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SystemFlavorMap methods
        pointcut systemFlavorMapMethods() :
                (execution(* java.awt.datatransfer.SystemFlavorMap.addFlavorForUnencodedNative(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.addUnencodedNativeForFlavor(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.decodeDataFlavor(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.getFlavorsForNative(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.getFlavorsForNatives(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.getNativesForFlavor(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.getNativesForFlavors(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.setFlavorsForNative(..)) ||
                        execution(* java.awt.datatransfer.SystemFlavorMap.setNativesForFlavor(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnsupportedFlavorException constructors
        pointcut unsupportedFlavorExceptionMethods() :
                (execution(java.awt.datatransfer.UnsupportedFlavorException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for desktop event constructors
        pointcut desktopEventConstructors() :
                (execution(java.awt.desktop.AboutEvent.new(..)) ||
                        execution(java.awt.desktop.AppForegroundEvent.new(..)) ||
                        execution(java.awt.desktop.AppHiddenEvent.new(..)) ||
                        execution(java.awt.desktop.AppReopenedEvent.new(..)) ||
                        execution(java.awt.desktop.OpenFilesEvent.new(..)) ||
                        execution(java.awt.desktop.OpenURIEvent.new(..)) ||
                        execution(java.awt.desktop.PreferencesEvent.new(..)) ||
                        execution(java.awt.desktop.PrintFilesEvent.new(..)) ||
                        execution(java.awt.desktop.QuitEvent.new(..)) ||
                        execution(java.awt.desktop.ScreenSleepEvent.new(..)) ||
                        execution(java.awt.desktop.SystemSleepEvent.new(..)) ||
                        execution(java.awt.desktop.UserSessionEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DragGestureEvent startDrag methods
        pointcut dragGestureEventStartDragMethods() :
                (execution(* java.awt.dnd.DragGestureEvent.startDrag(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DragGestureRecognizer addDragGestureListener method
        pointcut dragGestureRecognizerAddListenerMethods() :
                (execution(* java.awt.dnd.DragGestureRecognizer.addDragGestureListener(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DragSource methods
        pointcut dragSourceMethods() :
                (execution(* java.awt.dnd.DragSource.createDefaultDragGestureRecognizer(..)) ||
                        execution(* java.awt.dnd.DragSource.createDragGestureRecognizer(..)) ||
                        execution(* java.awt.dnd.DragSource.getDragThreshold(..)) ||
                        execution(* java.awt.dnd.DragSource.isDragImageSupported(..)) ||
                        execution(* java.awt.dnd.DragSource.startDrag(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DragSourceContext constructors and methods
        pointcut dragSourceContextMethods() :
                (execution(java.awt.dnd.DragSourceContext.new(..)) ||
                        execution(* java.awt.dnd.DragSourceContext.addDragSourceListener(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DropTarget constructors and methods
        pointcut dropTargetMethods() :
                (execution(java.awt.dnd.DropTarget.new(..)) ||
                        execution(* java.awt.dnd.DropTarget.addDropTargetListener(..)) ||
                        execution(* java.awt.dnd.DropTarget.dragEnter(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ActionEvent constructors and methods
        pointcut actionEventMethods() :
                (execution(java.awt.event.ActionEvent.new(..)) ||
                        execution(* java.awt.event.ActionEvent.paramString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AdjustmentEvent constructors
        pointcut adjustmentEventConstructors() :
                (execution(java.awt.event.AdjustmentEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ComponentEvent constructors
        pointcut componentEventConstructors() :
                (execution(java.awt.event.ComponentEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ContainerEvent constructors
        pointcut containerEventConstructors() :
                (execution(java.awt.event.ContainerEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FocusEvent constructors
        pointcut focusEventConstructors() :
                (execution(java.awt.event.FocusEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HierarchyEvent constructors
        pointcut hierarchyEventConstructors() :
                (execution(java.awt.event.HierarchyEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputEvent methods
        pointcut inputEventMethods() :
                (execution(* java.awt.event.InputEvent.getModifiersExText(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputMethodEvent constructors
        pointcut inputMethodEventConstructors() :
                (execution(java.awt.event.InputMethodEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InvocationEvent constructors
        pointcut invocationEventConstructors() :
                (execution(java.awt.event.InvocationEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ItemEvent constructors
        pointcut itemEventConstructors() :
                (execution(java.awt.event.ItemEvent.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for KeyEvent constructors and methods
        pointcut keyEventMethods() :
                (execution(java.awt.event.KeyEvent.new(..)) ||
                        execution(* java.awt.event.KeyEvent.getKeyModifiersText(..)) ||
                        execution(* java.awt.event.KeyEvent.getKeyText(..)) ||
                        execution(* java.awt.event.KeyEvent.paramString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MouseEvent constructors and methods
        pointcut mouseEventMethods() :
                (execution(java.awt.event.MouseEvent.new(..)) ||
                        execution(* java.awt.event.MouseEvent.getMouseModifiersText(..)) ||
                        execution(* java.awt.event.MouseEvent.paramString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MouseWheelEvent constructors and methods
        pointcut mouseWheelEventMethods() :
                (execution(java.awt.event.MouseWheelEvent.new(..)) ||
                        execution(* java.awt.event.MouseWheelEvent.paramString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PaintEvent constructors
        pointcut paintEventConstructors() :
                execution(java.awt.event.PaintEvent.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextEvent constructors
        pointcut textEventConstructors() :
                execution(java.awt.event.TextEvent.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for WindowEvent constructors
        pointcut windowEventConstructors() :
                execution(java.awt.event.WindowEvent.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for GlyphVector methods
        pointcut glyphVectorMethods() :
                (execution(* java.awt.font.GlyphVector.getGlyphOutline(..)) ||
                        execution(* java.awt.font.GlyphVector.getGlyphPixelBounds(..)) ||
                        execution(* java.awt.font.GlyphVector.getPixelBounds(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ImageGraphicAttribute constructors
        pointcut imageGraphicAttributeConstructors() :
                execution(java.awt.font.ImageGraphicAttribute.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LineBreakMeasurer constructors and methods
        pointcut lineBreakMeasurerMethods() :
                (execution(java.awt.font.LineBreakMeasurer.new(..)) ||
                        execution(* java.awt.font.LineBreakMeasurer.deleteChar(..)) ||
                        execution(* java.awt.font.LineBreakMeasurer.insertChar(..)) ||
                        execution(* java.awt.font.LineBreakMeasurer.nextLayout(..)) ||
                        execution(* java.awt.font.LineBreakMeasurer.nextOffset(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ShapeGraphicAttribute methods
        pointcut shapeGraphicAttributeMethods() :
                execution(* java.awt.font.ShapeGraphicAttribute.draw(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextLayout constructors and methods
        pointcut textLayoutMethods() :
                (execution(java.awt.font.TextLayout.new(..)) ||
                        execution(* java.awt.font.TextLayout.getJustifiedLayout(..)) ||
                        execution(* java.awt.font.TextLayout.getPixelBounds(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TextMeasurer constructors and methods
        pointcut textMeasurerMethods() :
                (execution(java.awt.font.TextMeasurer.new(..)) ||
                        execution(* java.awt.font.TextMeasurer.deleteChar(..)) ||
                        execution(* java.awt.font.TextMeasurer.getAdvanceBetween(..)) ||
                        execution(* java.awt.font.TextMeasurer.getLayout(..)) ||
                        execution(* java.awt.font.TextMeasurer.getLineBreakIndex(..)) ||
                        execution(* java.awt.font.TextMeasurer.insertChar(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AffineTransform methods
        pointcut affineTransformMethods() :
                (execution(* java.awt.geom.AffineTransform.createInverse(..)) ||
                        execution(* java.awt.geom.AffineTransform.inverseTransform(..)) ||
                        execution(* java.awt.geom.AffineTransform.invert(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Area constructors and methods
        pointcut areaMethods() :
                (execution(java.awt.geom.Area.new(..)) ||
                        execution(* java.awt.geom.Area.add(..)) ||
                        execution(* java.awt.geom.Area.clone(..)) ||
                        execution(* java.awt.geom.Area.createTransformedArea(..)) ||
                        execution(* java.awt.geom.Area.equals(..)) ||
                        execution(* java.awt.geom.Area.exclusiveOr(..)) ||
                        execution(* java.awt.geom.Area.intersect(..)) ||
                        execution(* java.awt.geom.Area.subtract(..)) ||
                        execution(* java.awt.geom.Area.transform(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CubicCurve2D methods
        pointcut cubicCurve2DMethods() :
                (execution(* java.awt.geom.CubicCurve2D.solveCubic(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for NoninvertibleTransformException constructors
        pointcut noninvertibleTransformExceptionConstructors() :
                execution(java.awt.geom.NoninvertibleTransformException.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputContext methods
        pointcut inputContextMethods() :
                execution(* java.awt.im.InputContext.getInstance(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AbstractMultiResolutionImage methods
        pointcut abstractMultiResolutionImageMethods() :
                (execution(* java.awt.image.AbstractMultiResolutionImage.getHeight(..)) ||
                        execution(* java.awt.image.AbstractMultiResolutionImage.getProperty(..)) ||
                        execution(* java.awt.image.AbstractMultiResolutionImage.getSource(..)) ||
                        execution(* java.awt.image.AbstractMultiResolutionImage.getWidth(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AffineTransformOp methods
        pointcut affineTransformOpMethods() :
                execution(* java.awt.image.AffineTransformOp.filter(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BaseMultiResolutionImage methods
        pointcut baseMultiResolutionImageMethods() :
                execution(* java.awt.image.BaseMultiResolutionImage.getResolutionVariant(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedImage methods
        pointcut bufferedImageMethods() :
                (execution(* java.awt.image.BufferedImage.createGraphics(..)) ||
                        execution(* java.awt.image.BufferedImage.getGraphics(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ColorConvertOp methods
        pointcut colorConvertOpMethods() :
                (execution(* java.awt.image.ColorConvertOp.createCompatibleDestImage(..)) ||
                        execution(* java.awt.image.ColorConvertOp.createCompatibleDestRaster(..)) ||
                        execution(* java.awt.image.ColorConvertOp.filter(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ConvolveOp methods
        pointcut convolveOpMethods() :
                execution(* java.awt.image.ConvolveOp.filter(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LookupOp methods
        pointcut lookupOpMethods() :
                execution(* java.awt.image.LookupOp.filter(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PixelGrabber constructors
        pointcut pixelGrabberConstructors() :
                execution(java.awt.image.PixelGrabber.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RescaleOp methods
        pointcut rescaleOpMethods() :
                execution(* java.awt.image.RescaleOp.filter(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for VolatileImage methods
        pointcut volatileImageMethods() :
                (execution(* java.awt.image.VolatileImage.getGraphics(..)) ||
                        execution(* java.awt.image.VolatileImage.getSource(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrinterException and its subclasses constructors
        pointcut printerExceptionConstructors() :
                (execution(java.awt.print.PrinterAbortException.new(..)) ||
                        execution(java.awt.print.PrinterException.new(..)) ||
                        execution(java.awt.print.PrinterIOException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrinterJob methods
        pointcut printerJobMethods() :
                (execution(* java.awt.print.PrinterJob.defaultPage(..)) ||
                        execution(* java.awt.print.PrinterJob.getPageFormat(..)) ||
                        execution(* java.awt.print.PrinterJob.lookupPrintServices(..)) ||
                        execution(* java.awt.print.PrinterJob.lookupStreamPrintServices(..)) ||
                        execution(* java.awt.print.PrinterJob.pageDialog(..)) ||
                        execution(* java.awt.print.PrinterJob.print(..)) ||
                        execution(* java.awt.print.PrinterJob.printDialog(..)) ||
                        execution(* java.awt.print.PrinterJob.setPrintService(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BeanDescriptor constructors
        pointcut beanDescriptorConstructors() :
                execution(java.beans.BeanDescriptor.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Beans methods
        pointcut beansMethods() :
                (execution(* java.beans.Beans.instantiate(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Encoder methods
        pointcut encoderMethods() :
                (execution(* java.beans.Encoder.get(..)) ||
                        execution(* java.beans.Encoder.getPersistenceDelegate(..)) ||
                        execution(* java.beans.Encoder.remove(..)) ||
                        execution(* java.beans.Encoder.writeExpression(..)) ||
                        execution(* java.beans.Encoder.writeStatement(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for EventHandler constructors and methods
        pointcut eventHandlerMethods() :
                (execution(java.beans.EventHandler.new(..)) ||
                        execution(* java.beans.EventHandler.create(..)) ||
                        execution(* java.beans.EventHandler.invoke(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for EventSetDescriptor constructors
        pointcut eventSetDescriptorConstructors() :
                (execution(java.beans.EventSetDescriptor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for EventSetDescriptor methods
        pointcut eventSetDescriptorMethods() :
                (execution(* java.beans.EventSetDescriptor.getAddListenerMethod(..)) ||
                        execution(* java.beans.EventSetDescriptor.getGetListenerMethod(..)) ||
                        execution(* java.beans.EventSetDescriptor.getListenerMethods(..)) ||
                        execution(* java.beans.EventSetDescriptor.getRemoveListenerMethod(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Expression constructors
        pointcut expressionConstructors() :
                (execution(java.beans.Expression.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Expression methods
        pointcut expressionMethods() :
                (execution(* java.beans.Expression.execute(..)) ||
                        execution(* java.beans.Expression.getValue(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FeatureDescriptor method
        pointcut featureDescriptorMethods() :
                (execution(* java.beans.FeatureDescriptor.toString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IndexedPropertyDescriptor constructors
        pointcut indexedPropertyDescriptorConstructors() :
                (execution(java.beans.IndexedPropertyDescriptor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IndexedPropertyDescriptor methods
        pointcut indexedPropertyDescriptorMethods() :
                (execution(* java.beans.IndexedPropertyDescriptor.equals(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.getIndexedPropertyType(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.getIndexedReadMethod(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.getIndexedWriteMethod(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.hashCode(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.setIndexedReadMethod(..)) ||
                        execution(* java.beans.IndexedPropertyDescriptor.setIndexedWriteMethod(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IntrospectionException constructor
        pointcut introspectionExceptionConstructor() :
                (execution(java.beans.IntrospectionException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Introspector methods
        pointcut introspectorMethods() :
                (execution(* java.beans.Introspector.getBeanInfo(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MethodDescriptor constructors
        pointcut methodDescriptorConstructors() :
                (execution(java.beans.MethodDescriptor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MethodDescriptor methods
        pointcut methodDescriptorMethods() :
                (execution(* java.beans.MethodDescriptor.getMethod(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PersistenceDelegate methods
        pointcut persistenceDelegateMethods() :
                (execution(* java.beans.PersistenceDelegate.writeObject(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PropertyChangeSupport methods
        pointcut propertyChangeSupportMethods() :
                (execution(* java.beans.PropertyChangeSupport.fireIndexedPropertyChange(..)) ||
                        execution(* java.beans.PropertyChangeSupport.firePropertyChange(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PropertyDescriptor constructors
        pointcut propertyDescriptorConstructors() :
                (execution(java.beans.PropertyDescriptor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PropertyDescriptor methods
        pointcut propertyDescriptorMethods() :
                (execution(* java.beans.PropertyDescriptor.equals(..)) ||
                        execution(* java.beans.PropertyDescriptor.getPropertyType(..)) ||
                        execution(* java.beans.PropertyDescriptor.getReadMethod(..)) ||
                        execution(* java.beans.PropertyDescriptor.getWriteMethod(..)) ||
                        execution(* java.beans.PropertyDescriptor.hashCode(..)) ||
                        execution(* java.beans.PropertyDescriptor.setPropertyEditorClass(..)) ||
                        execution(* java.beans.PropertyDescriptor.setReadMethod(..)) ||
                        execution(* java.beans.PropertyDescriptor.setWriteMethod(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PropertyVetoException constructor
        pointcut propertyVetoExceptionConstructor() :
                (execution(java.beans.PropertyVetoException.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SimpleBeanInfo methods
        pointcut simpleBeanInfoMethods() :
                (execution(* java.beans.SimpleBeanInfo.getIcon(..)) ||
                        execution(* java.beans.SimpleBeanInfo.loadImage(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Statement constructors
        pointcut statementConstructors() :
                (execution(java.beans.Statement.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Statement execute method
        pointcut statementExecuteMethod() :
                (execution(* java.beans.Statement.execute(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XMLDecoder constructors
        pointcut xmlDecoderConstructors() :
                (execution(java.beans.XMLDecoder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XMLDecoder methods
        pointcut xmlDecoderMethods() :
                (execution(* java.beans.XMLDecoder.close(..)) ||
                        execution(* java.beans.XMLDecoder.readObject(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XMLEncoder constructors
        pointcut xmlEncoderConstructors() :
                (execution(java.beans.XMLEncoder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XMLEncoder methods
        pointcut xmlEncoderMethods() :
                (execution(* java.beans.XMLEncoder.close(..)) ||
                        execution(* java.beans.XMLEncoder.flush(..)) ||
                        execution(* java.beans.XMLEncoder.setOwner(..)) ||
                        execution(* java.beans.XMLEncoder.writeExpression(..)) ||
                        execution(* java.beans.XMLEncoder.writeObject(..)) ||
                        execution(* java.beans.XMLEncoder.writeStatement(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BeanContextChildSupport methods
        pointcut beanContextChildSupportMethods() :
                (execution(* java.beans.beancontext.BeanContextChildSupport.firePropertyChange(..)) ||
                        execution(* java.beans.beancontext.BeanContextChildSupport.setBeanContext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BeanContextServicesSupport methods
        pointcut beanContextServicesSupportMethods() :
                (execution(* java.beans.beancontext.BeanContextServicesSupport.getService(..)) ||
                        execution(* java.beans.beancontext.BeanContextServicesSupport.revokeService(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BeanContextSupport methods
        pointcut beanContextSupportMethods() :
                (execution(* java.beans.beancontext.BeanContextSupport.instantiateChild(..)) ||
                        execution(* java.beans.beancontext.BeanContextSupport.readChildren(..)) ||
                        execution(* java.beans.beancontext.BeanContextSupport.setDesignTime(..)) ||
                        execution(* java.beans.beancontext.BeanContextSupport.setLocale(..)) ||
                        execution(* java.beans.beancontext.BeanContextSupport.vetoableChange(..)) ||
                        execution(* java.beans.beancontext.BeanContextSupport.writeChildren(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedInputStream read methods
        pointcut bufferedInputStreamReadMethods() :
                (execution(* java.io.BufferedInputStream.read(..)) ||
                        execution(* java.io.BufferedInputStream.skip(long)) ||
                        execution(* java.io.BufferedInputStream.available()) ||
                        execution(* java.io.BufferedInputStream.reset()) ||
                        execution(* java.io.BufferedInputStream.transferTo(java.io.OutputStream))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedInputStream close method
        pointcut bufferedInputStreamCloseMethod() :
                execution(* java.io.BufferedInputStream.close(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedOutputStream constructors
        pointcut bufferedOutputStreamConstructors() :
                execution(java.io.BufferedOutputStream.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedOutputStream write methods
        pointcut bufferedOutputStreamWriteMethods() :
                (execution(* java.io.BufferedOutputStream.write(..)) ||
                        execution(* java.io.BufferedOutputStream.flush())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedReader read methods
        pointcut bufferedReaderReadMethods() :
                (execution(* java.io.BufferedReader.read(..)) ||
                        execution(* java.io.BufferedReader.readLine(..)) ||
                        execution(* java.io.BufferedReader.ready()) ||
                        execution(* java.io.BufferedReader.reset()) ||
                        execution(* java.io.BufferedReader.skip(long)) ||
                        execution(* java.io.BufferedReader.mark(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedReader close method
        pointcut bufferedReaderCloseMethod() :
                execution(* java.io.BufferedReader.close(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedWriter write methods
        pointcut bufferedWriterWriteMethods() :
                (execution(* java.io.BufferedWriter.write(..)) ||
                        execution(* java.io.BufferedWriter.flush()) ||
                        execution(* java.io.BufferedWriter.newLine())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BufferedWriter close method
        pointcut bufferedWriterCloseMethod() :
                execution(* java.io.BufferedWriter.close(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ByteArrayOutputStream constructors
        pointcut byteArrayOutputStreamConstructors() :
                execution(java.io.ByteArrayOutputStream.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ByteArrayOutputStream write methods
        pointcut byteArrayOutputStreamWriteMethods() :
                (execution(* java.io.ByteArrayOutputStream.writeBytes(..)) ||
                        execution(* java.io.ByteArrayOutputStream.writeTo(java.io.OutputStream))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CharArrayReader read methods
        pointcut charArrayReaderReadMethods() :
                (execution(* java.io.CharArrayReader.read(..)) ||
                        execution(* java.io.CharArrayReader.ready()) ||
                        execution(* java.io.CharArrayReader.mark(int)) ||
                        execution(* java.io.CharArrayReader.reset()) ||
                        execution(* java.io.CharArrayReader.skip(long))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CharArrayWriter writeTo method
        pointcut charArrayWriterWriteToMethod() :
                execution(* java.io.CharArrayWriter.writeTo(java.io.Writer)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DataInputStream read methods
        pointcut dataInputStreamReadMethods() :
                (execution(* java.io.DataInputStream.read(..)) ||
                        execution(* java.io.DataInputStream.readBoolean()) ||
                        execution(* java.io.DataInputStream.readByte()) ||
                        execution(* java.io.DataInputStream.readChar()) ||
                        execution(* java.io.DataInputStream.readDouble()) ||
                        execution(* java.io.DataInputStream.readFloat()) ||
                        execution(* java.io.DataInputStream.readFully(..)) ||
                        execution(* java.io.DataInputStream.readInt()) ||
                        execution(* java.io.DataInputStream.readLine()) ||
                        execution(* java.io.DataInputStream.readLong()) ||
                        execution(* java.io.DataInputStream.readShort()) ||
                        execution(* java.io.DataInputStream.readUTF()) ||
                        execution(* java.io.DataInputStream.readUnsignedByte()) ||
                        execution(* java.io.DataInputStream.readUnsignedShort()) ||
                        execution(* java.io.DataInputStream.skipBytes(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DataOutputStream constructors
        pointcut dataOutputStreamConstructors() :
                execution(java.io.DataOutputStream.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DataOutputStream write methods
        pointcut dataOutputStreamWriteMethods() :
                (execution(* java.io.DataOutputStream.write(..)) ||
                        execution(* java.io.DataOutputStream.writeBoolean(boolean)) ||
                        execution(* java.io.DataOutputStream.writeByte(int)) ||
                        execution(* java.io.DataOutputStream.writeBytes(java.lang.String)) ||
                        execution(* java.io.DataOutputStream.writeChar(int)) ||
                        execution(* java.io.DataOutputStream.writeChars(java.lang.String)) ||
                        execution(* java.io.DataOutputStream.writeDouble(double)) ||
                        execution(* java.io.DataOutputStream.writeFloat(float)) ||
                        execution(* java.io.DataOutputStream.writeInt(int)) ||
                        execution(* java.io.DataOutputStream.writeLong(long)) ||
                        execution(* java.io.DataOutputStream.writeShort(int)) ||
                        execution(* java.io.DataOutputStream.writeUTF(java.lang.String)) ||
                        execution(* java.io.DataOutputStream.flush())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File execute methods
        pointcut fileExecuteMethods() :
                (execution(* java.io.File.canExecute()) ||
                        execution(* java.io.File.setExecutable(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File read methods
        pointcut fileReadMethods() :
                (execution(* java.io.File.canRead()) ||
                        execution(* java.io.File.getCanonicalFile()) ||
                        execution(* java.io.File.getCanonicalPath()) ||
                        execution(* java.io.File.getFreeSpace()) ||
                        execution(* java.io.File.getTotalSpace()) ||
                        execution(* java.io.File.getUsableSpace()) ||
                        execution(* java.io.File.isDirectory()) ||
                        execution(* java.io.File.isFile()) ||
                        execution(* java.io.File.isHidden()) ||
                        execution(* java.io.File.lastModified()) ||
                        execution(* java.io.File.length()) ||
                        execution(* java.io.File.list(..)) ||
                        execution(* java.io.File.listFiles(..)) ||
                        execution(* java.io.File.exists())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File write methods
        pointcut fileWriteMethods() :
                (execution(* java.io.File.canWrite()) ||
                        execution(* java.io.File.createNewFile()) ||
                        execution(* java.io.File.createTempFile(..)) ||
                        execution(* java.io.File.setLastModified(long)) ||
                        execution(* java.io.File.setReadOnly()) ||
                        execution(* java.io.File.setReadable(..)) ||
                        execution(* java.io.File.setWritable(..)) ||
                        execution(* java.io.File.mkdir()) ||
                        execution(* java.io.File.mkdirs()) ||
                        execution(* java.io.File.renameTo(java.io.File))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File delete methods
        pointcut fileDeleteMethods() :
                (execution(* java.io.File.delete()) ||
                        execution(* java.io.File.deleteOnExit())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File conversion methods
        pointcut fileConversionMethods() :
                (execution(* java.io.File.toPath()) ||
                        execution(* java.io.File.toURI()) ||
                        execution(* java.io.File.toURL())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileInputStream read methods
        pointcut fileInputStreamReadMethods() :
                (execution(* java.io.FileInputStream.readAllBytes()) ||
                        execution(* java.io.FileInputStream.readNBytes(int)) ||
                        execution(* java.io.FileInputStream.transferTo(java.io.OutputStream))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileInputStream resource methods
        pointcut fileInputStreamResourceMethods() :
                (execution(* java.io.FileInputStream.getChannel()) ||
                        execution(* java.io.FileInputStream.getFD()) ||
                        execution(* java.io.FileInputStream.close())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileOutputStream write methods
        pointcut fileOutputStreamWriteMethods() :
                (execution(* java.io.FileOutputStream.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileOutputStream resource methods
        pointcut fileOutputStreamResourceMethods() :
                (execution(* java.io.FileOutputStream.getChannel()) ||
                        execution(* java.io.FileOutputStream.getFD()) ||
                        execution(* java.io.FileOutputStream.close())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileReader read methods
        pointcut fileReaderReadMethods() :
                (execution(* java.io.FileReader.read(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileWriter write methods
        pointcut fileWriterWriteMethods() :
                (execution(* java.io.FileWriter.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterInputStream read methods
        pointcut filterInputStreamReadMethods() :
                (execution(* java.io.FilterInputStream.read(..)) ||
                        execution(* java.io.FilterInputStream.skip(long)) ||
                        execution(* java.io.FilterInputStream.available())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterInputStream resource methods
        pointcut filterInputStreamResourceMethods() :
                (execution(* java.io.FilterInputStream.close()) ||
                        execution(* java.io.FilterInputStream.reset())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterOutputStream write methods
        pointcut filterOutputStreamWriteMethods() :
                (execution(* java.io.FilterOutputStream.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterOutputStream resource methods
        pointcut filterOutputStreamResourceMethods() :
                (execution(* java.io.FilterOutputStream.close()) ||
                        execution(* java.io.FilterOutputStream.flush())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterReader read methods
        pointcut filterReaderReadMethods() :
                (execution(* java.io.FilterReader.read(..)) ||
                        execution(* java.io.FilterReader.skip(long)) ||
                        execution(* java.io.FilterReader.ready())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterReader resource methods
        pointcut filterReaderResourceMethods() :
                (execution(* java.io.FilterReader.close()) ||
                        execution(* java.io.FilterReader.mark(int)) ||
                        execution(* java.io.FilterReader.reset())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterWriter write methods
        pointcut filterWriterWriteMethods() :
                (execution(* java.io.FilterWriter.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterWriter resource methods
        pointcut filterWriterResourceMethods() :
                (execution(* java.io.FilterWriter.close()) ||
                        execution(* java.io.FilterWriter.flush())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputStream read methods
        pointcut inputStreamReadMethods() :
                (execution(* java.io.InputStream.read(..)) ||
                        execution(* java.io.InputStream.skip(long)) ||
                        execution(* java.io.InputStream.reset()) ||
                        execution(* java.io.InputStream.transferTo(java.io.OutputStream))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputStreamReader read methods
        pointcut inputStreamReaderReadMethods() :
                (execution(* java.io.InputStreamReader.read(..)) ||
                        execution(* java.io.InputStreamReader.ready())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputStreamReader resource methods
        pointcut inputStreamReaderResourceMethods() :
                (execution(* java.io.InputStreamReader.close())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LineNumberInputStream read methods
        pointcut lineNumberInputStreamReadMethods() :
                (execution(* java.io.LineNumberInputStream.read(..)) ||
                        execution(* java.io.LineNumberInputStream.skip(long)) ||
                        execution(* java.io.LineNumberInputStream.reset())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LineNumberReader read methods
        pointcut lineNumberReaderReadMethods() :
                (execution(* java.io.LineNumberReader.read(..)) ||
                        execution(* java.io.LineNumberReader.readLine()) ||
                        execution(* java.io.LineNumberReader.ready())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LineNumberReader resource methods
        pointcut lineNumberReaderResourceMethods() :
                (execution(* java.io.LineNumberReader.close()) ||
                        execution(* java.io.LineNumberReader.mark(int)) ||
                        execution(* java.io.LineNumberReader.reset())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream read methods
        pointcut objectInputStreamReadMethods() :
                (execution(* java.io.ObjectInputStream.read(..)) ||
                        execution(* java.io.ObjectInputStream.readBoolean()) ||
                        execution(* java.io.ObjectInputStream.readByte()) ||
                        execution(* java.io.ObjectInputStream.readChar()) ||
                        execution(* java.io.ObjectInputStream.readDouble()) ||
                        execution(* java.io.ObjectInputStream.readFields()) ||
                        execution(* java.io.ObjectInputStream.readFloat()) ||
                        execution(* java.io.ObjectInputStream.readFully(..)) ||
                        execution(* java.io.ObjectInputStream.readInt()) ||
                        execution(* java.io.ObjectInputStream.readLine()) ||
                        execution(* java.io.ObjectInputStream.readLong()) ||
                        execution(* java.io.ObjectInputStream.readObject()) ||
                        execution(* java.io.ObjectInputStream.readShort()) ||
                        execution(* java.io.ObjectInputStream.readUTF()) ||
                        execution(* java.io.ObjectInputStream.readUnshared()) ||
                        execution(* java.io.ObjectInputStream.readUnsignedByte()) ||
                        execution(* java.io.ObjectInputStream.readUnsignedShort()) ||
                        execution(* java.io.ObjectInputStream.skipBytes(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream resource methods
        pointcut objectInputStreamResourceMethods() :
                (execution(* java.io.ObjectInputStream.close()) ||
                        execution(* java.io.ObjectInputStream.available()) ||
                        execution(* java.io.ObjectInputStream.defaultReadObject()) ||
                        execution(* java.io.ObjectInputStream.registerValidation(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectOutputStream write methods
        pointcut objectOutputStreamWriteMethods() :
                (execution(* java.io.ObjectOutputStream.write(..)) ||
                        execution(* java.io.ObjectOutputStream.writeBoolean(boolean)) ||
                        execution(* java.io.ObjectOutputStream.writeByte(int)) ||
                        execution(* java.io.ObjectOutputStream.writeBytes(java.lang.String)) ||
                        execution(* java.io.ObjectOutputStream.writeChar(int)) ||
                        execution(* java.io.ObjectOutputStream.writeChars(java.lang.String)) ||
                        execution(* java.io.ObjectOutputStream.writeDouble(double)) ||
                        execution(* java.io.ObjectOutputStream.writeFields()) ||
                        execution(* java.io.ObjectOutputStream.writeFloat(float)) ||
                        execution(* java.io.ObjectOutputStream.writeInt(int)) ||
                        execution(* java.io.ObjectOutputStream.writeLong(long)) ||
                        execution(* java.io.ObjectOutputStream.writeObject(java.lang.Object)) ||
                        execution(* java.io.ObjectOutputStream.writeShort(int)) ||
                        execution(* java.io.ObjectOutputStream.writeUTF(java.lang.String)) ||
                        execution(* java.io.ObjectOutputStream.writeUnshared(java.lang.Object))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectOutputStream resource methods
        pointcut objectOutputStreamResourceMethods() :
                (execution(* java.io.ObjectOutputStream.close()) ||
                        execution(* java.io.ObjectOutputStream.defaultWriteObject()) ||
                        execution(* java.io.ObjectOutputStream.flush()) ||
                        execution(* java.io.ObjectOutputStream.putFields()) ||
                        execution(* java.io.ObjectOutputStream.reset())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for OutputStream write methods
        pointcut outputStreamWriteMethods() :
                (execution(* java.io.OutputStream.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for OutputStream resource methods
        pointcut outputStreamResourceMethods() :
                (execution(* java.io.OutputStream.nullOutputStream()) ||
                        execution(* java.io.OutputStreamWriter.close()) ||
                        execution(* java.io.OutputStreamWriter.flush())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedInputStream read methods
        pointcut pipedInputStreamReadMethods() :
                (execution(* java.io.PipedInputStream.read(..)) ||
                        execution(* java.io.PipedInputStream.read(byte[], int, int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedOutputStream write methods
        pointcut pipedOutputStreamWriteMethods() :
                (execution(* java.io.PipedOutputStream.write(..)) ||
                        execution(* java.io.PipedOutputStream.write(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedReader read methods
        pointcut pipedReaderReadMethods() :
                (execution(* java.io.PipedReader.read(..)) ||
                        execution(* java.io.PipedReader.read(char[], int, int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedWriter write methods
        pointcut pipedWriterWriteMethods() :
                (execution(* java.io.PipedWriter.write(..)) ||
                        execution(* java.io.PipedWriter.write(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintStream write methods
        pointcut printStreamWriteMethods() :
                (execution(* java.io.PrintStream.write(..)) ||
                        execution(* java.io.PrintStream.print(..)) ||
                        execution(* java.io.PrintStream.println(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedInputStream read methods
        pointcut pipedInputStreamReadMethods() :
                (execution(* java.io.PipedInputStream.read(..)) ||
                        execution(* java.io.PipedInputStream.read(byte[], int, int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedOutputStream write methods
        pointcut pipedOutputStreamWriteMethods() :
                (execution(* java.io.PipedOutputStream.write(..)) ||
                        execution(* java.io.PipedOutputStream.write(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedReader read methods
        pointcut pipedReaderReadMethods() :
                (execution(* java.io.PipedReader.read(..)) ||
                        execution(* java.io.PipedReader.read(char[], int, int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PipedWriter write methods
        pointcut pipedWriterWriteMethods() :
                (execution(* java.io.PipedWriter.write(..)) ||
                        execution(* java.io.PipedWriter.write(int))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintStream write methods
        pointcut printStreamWriteMethods() :
                (execution(* java.io.PrintStream.write(..)) ||
                        execution(* java.io.PrintStream.print(..)) ||
                        execution(* java.io.PrintStream.println(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintWriter write methods
        pointcut printWriterWriteMethods() :
                (execution(* java.io.PrintWriter.write(..)) ||
                        execution(* java.io.PrintWriter.print(..)) ||
                        execution(* java.io.PrintWriter.println(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PushbackInputStream read methods
        pointcut pushbackInputStreamReadMethods() :
                (execution(* java.io.PushbackInputStream.read(..)) ||
                        execution(* java.io.PushbackInputStream.transferTo(..)) ||
                        execution(* java.io.PushbackInputStream.unread(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PushbackReader read methods
        pointcut pushbackReaderReadMethods() :
                (execution(* java.io.PushbackReader.read(..)) ||
                        execution(* java.io.PushbackReader.unread(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RandomAccessFile read methods
        pointcut randomAccessFileReadMethods() :
                (execution(* java.io.RandomAccessFile.read(..)) ||
                        execution(* java.io.RandomAccessFile.readFully(..)) ||
                        execution(* java.io.RandomAccessFile.readLine()) ||
                        execution(* java.io.RandomAccessFile.readUnsignedByte()) ||
                        execution(* java.io.RandomAccessFile.readUnsignedShort()) ||
                        execution(* java.io.RandomAccessFile.seek(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RandomAccessFile write methods
        pointcut randomAccessFileWriteMethods() :
                execution(* java.io.RandomAccessFile.writeUTF(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Reader read methods
        pointcut readerReadMethods() :
                (execution(* java.io.Reader.read(..)) ||
                        execution(* java.io.Reader.mark(..)) ||
                        execution(* java.io.Reader.reset(..)) ||
                        execution(* java.io.Reader.skip(..)) ||
                        execution(* java.io.Reader.transferTo(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SequenceInputStream read methods
        pointcut sequenceInputStreamReadMethods() :
                (execution(* java.io.SequenceInputStream.read(..)) ||
                        execution(* java.io.SequenceInputStream.available()) ||
                        execution(* java.io.SequenceInputStream.close()) ||
                        execution(* java.io.SequenceInputStream.transferTo(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for StringReader read methods
        pointcut stringReaderReadMethods() :
                (execution(* java.io.StringReader.read(..)) ||
                        execution(* java.io.StringReader.mark(..)) ||
                        execution(* java.io.StringReader.reset(..)) ||
                        execution(* java.io.StringReader.skip(..)) ||
                        execution(* java.io.StringReader.ready())) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Writer write methods
        pointcut writerWriteMethods() :
                (execution(* java.io.Writer.write(..)) ||
                        execution(* java.io.Writer.append(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Authenticator read methods
        pointcut authenticatorReadMethods() :
                (execution(* java.net.Authenticator.getDefault(..)) ||
                        execution(* java.net.Authenticator.requestPasswordAuthentication(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Authenticator write methods
        pointcut authenticatorWriteMethods() :
                execution(* java.net.Authenticator.setDefault(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DatagramSocket initialization methods
        pointcut datagramSocketInitMethods() :
                execution(java.net.DatagramSocket.new(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DatagramSocket write methods
        pointcut datagramSocketWriteMethods() :
                execution(* java.net.DatagramSocket.setDatagramSocketImplFactory(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpURLConnection read methods
        pointcut httpURLConnectionReadMethods() :
                (execution(* java.net.HttpURLConnection.getHeaderFieldDate(..)) ||
                        execution(* java.net.HttpURLConnection.getPermission(..)) ||
                        execution(* java.net.HttpURLConnection.getResponseCode(..)) ||
                        execution(* java.net.HttpURLConnection.getResponseMessage(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpURLConnection write methods
        pointcut httpURLConnectionWriteMethods() :
                execution(* java.net.HttpURLConnection.setRequestMethod(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IDN read methods
        pointcut idnReadMethods() :
                (execution(* java.net.IDN.toASCII(..)) ||
                        execution(* java.net.IDN.toUnicode(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InetAddress read methods
        pointcut inetAddressReadMethods() :
                (execution(* java.net.Inet6Address.getByAddress(..)) ||
                        execution(* java.net.InetAddress.getAllByName(..)) ||
                        execution(* java.net.InetAddress.getByAddress(..)) ||
                        execution(* java.net.InetAddress.getByName(..)) ||
                        execution(* java.net.InetAddress.getCanonicalHostName(..)) ||
                        execution(* java.net.InetAddress.getHostName(..)) ||
                        execution(* java.net.InetAddress.getLocalHost(..)) ||
                        execution(* java.net.InetSocketAddress.getHostName(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for JarURLConnection read methods
        pointcut jarURLConnectionReadMethods() :
                (execution(* java.net.JarURLConnection.getAttributes(..)) ||
                        execution(* java.net.JarURLConnection.getCertificates(..)) ||
                        execution(* java.net.JarURLConnection.getJarEntry(..)) ||
                        execution(* java.net.JarURLConnection.getMainAttributes(..)) ||
                        execution(* java.net.JarURLConnection.getManifest(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MulticastSocket read methods
        pointcut multicastSocketReadMethods() :
                (execution(* java.net.MulticastSocket.getInterface(..)) ||
                        execution(* java.net.MulticastSocket.getLoopbackMode(..)) ||
                        execution(* java.net.MulticastSocket.getNetworkInterface(..)) ||
                        execution(* java.net.MulticastSocket.getTTL(..)) ||
                        execution(* java.net.MulticastSocket.getTimeToLive(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MulticastSocket write methods
        pointcut multicastSocketWriteMethods() :
                (execution(* java.net.MulticastSocket.setInterface(..)) ||
                        execution(* java.net.MulticastSocket.setLoopbackMode(..)) ||
                        execution(* java.net.MulticastSocket.setNetworkInterface(..)) ||
                        execution(* java.net.MulticastSocket.setTTL(..)) ||
                        execution(* java.net.MulticastSocket.setTimeToLive(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServerSocket read methods
        pointcut serverSocketReadMethods() :
                (execution(* java.net.ServerSocket.accept(..)) ||
                        execution(* java.net.ServerSocket.getInetAddress(..)) ||
                        execution(* java.net.ServerSocket.getLocalPort(..)) ||
                        execution(* java.net.ServerSocket.getLocalSocketAddress(..)) ||
                        execution(* java.net.ServerSocket.getOption(..)) ||
                        execution(* java.net.ServerSocket.getReceiveBufferSize(..)) ||
                        execution(* java.net.ServerSocket.getReuseAddress(..)) ||
                        execution(* java.net.ServerSocket.getSoTimeout(..)) ||
                        execution(* java.net.ServerSocket.supportedOptions(..)) ||
                        execution(* java.net.ServerSocket.toString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServerSocket write methods
        pointcut serverSocketWriteMethods() :
                (execution(* java.net.ServerSocket.bind(..)) ||
                        execution(* java.net.ServerSocket.close(..)) ||
                        execution(* java.net.ServerSocket.setOption(..)) ||
                        execution(* java.net.ServerSocket.setReceiveBufferSize(..)) ||
                        execution(* java.net.ServerSocket.setReuseAddress(..)) ||
                        execution(* java.net.ServerSocket.setSoTimeout(..)) ||
                        execution(* java.net.ServerSocket.setSocketFactory(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Socket read methods
        pointcut socketReadMethods() :
                (execution(* java.net.Socket.getInetAddress(..)) ||
                        execution(* java.net.Socket.getInputStream(..)) ||
                        execution(* java.net.Socket.getKeepAlive(..)) ||
                        execution(* java.net.Socket.getLocalAddress(..)) ||
                        execution(* java.net.Socket.getLocalPort(..)) ||
                        execution(* java.net.Socket.getLocalSocketAddress(..)) ||
                        execution(* java.net.Socket.getOOBInline(..)) ||
                        execution(* java.net.Socket.getOption(..)) ||
                        execution(* java.net.Socket.getOutputStream(..)) ||
                        execution(* java.net.Socket.getPort(..)) ||
                        execution(* java.net.Socket.getReceiveBufferSize(..)) ||
                        execution(* java.net.Socket.getRemoteSocketAddress(..)) ||
                        execution(* java.net.Socket.getReuseAddress(..)) ||
                        execution(* java.net.Socket.getSendBufferSize(..)) ||
                        execution(* java.net.Socket.getSoLinger(..)) ||
                        execution(* java.net.Socket.getSoTimeout(..)) ||
                        execution(* java.net.Socket.getTcpNoDelay(..)) ||
                        execution(* java.net.Socket.getTrafficClass(..)) ||
                        execution(* java.net.Socket.supportedOptions(..)) ||
                        execution(* java.net.Socket.toString(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Socket write methods
        pointcut socketWriteMethods() :
                (execution(* java.net.Socket.bind(..)) ||
                        execution(* java.net.Socket.close(..)) ||
                        execution(* java.net.Socket.connect(..)) ||
                        execution(* java.net.Socket.sendUrgentData(..)) ||
                        execution(* java.net.Socket.setKeepAlive(..)) ||
                        execution(* java.net.Socket.setOOBInline(..)) ||
                        execution(* java.net.Socket.setOption(..)) ||
                        execution(* java.net.Socket.setReceiveBufferSize(..)) ||
                        execution(* java.net.Socket.setReuseAddress(..)) ||
                        execution(* java.net.Socket.setSendBufferSize(..)) ||
                        execution(* java.net.Socket.setSoLinger(..)) ||
                        execution(* java.net.Socket.setSoTimeout(..)) ||
                        execution(* java.net.Socket.setSocketImplFactory(..)) ||
                        execution(* java.net.Socket.setTcpNoDelay(..)) ||
                        execution(* java.net.Socket.setTrafficClass(..)) ||
                        execution(* java.net.Socket.shutdownInput(..)) ||
                        execution(* java.net.Socket.shutdownOutput(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URI read methods
        pointcut uriReadMethods() :
                (execution(* java.net.URI.getRawSchemeSpecificPart(..)) ||
                        execution(* java.net.URI.getSchemeSpecificPart(..)) ||
                        execution(* java.net.URI.parseServerAuthority(..)) ||
                        execution(* java.net.URI.resolve(..)) ||
                        execution(* java.net.URI.toASCIIString(..)) ||
                        execution(* java.net.URI.toURL(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URL read methods
        pointcut urlReadMethods() :
                (execution(* java.net.URL.getContent(..)) ||
                        execution(* java.net.URL.hashCode(..)) ||
                        execution(* java.net.URL.openConnection(..)) ||
                        execution(* java.net.URL.openStream(..)) ||
                        execution(* java.net.URL.sameFile(..)) ||
                        execution(* java.net.URL.toURI(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URLClassLoader read methods
        pointcut urlClassLoaderReadMethods() :
                (execution(* java.net.URLClassLoader.findResource(..)) ||
                        execution(* java.net.URLClassLoader.getResourceAsStream(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URLConnection read methods
        pointcut urlConnectionReadMethods() :
                (execution(* java.net.URLConnection.getContent(..)) ||
                        execution(* java.net.URLConnection.getContentEncoding(..)) ||
                        execution(* java.net.URLConnection.getContentLength(..)) ||
                        execution(* java.net.URLConnection.getContentLengthLong(..)) ||
                        execution(* java.net.URLConnection.getContentType(..)) ||
                        execution(* java.net.URLConnection.getDate(..)) ||
                        execution(* java.net.URLConnection.getExpiration(..)) ||
                        execution(* java.net.URLConnection.getHeaderFieldDate(..)) ||
                        execution(* java.net.URLConnection.getHeaderFieldInt(..)) ||
                        execution(* java.net.URLConnection.getHeaderFieldLong(..)) ||
                        execution(* java.net.URLConnection.getInputStream(..)) ||
                        execution(* java.net.URLConnection.getLastModified(..)) ||
                        execution(* java.net.URLConnection.guessContentTypeFromStream(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpRequest$BodyPublishers write methods
        pointcut httpRequestBodyPublishersWriteMethods() :
                execution(* java.net.http.HttpRequest$BodyPublishers.ofFile(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpResponse$BodyHandlers read methods
        pointcut httpResponseBodyHandlersReadMethods() :
                (execution(* java.net.http.HttpResponse$BodyHandlers.ofFile(..)) ||
                        execution(* java.net.http.HttpResponse$BodyHandlers.ofFileDownload(..)) ||
                        execution(* java.net.http.HttpResponse$BodyHandlers.ofPublisher(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpResponse$BodySubscribers read methods
        pointcut httpResponseBodySubscribersReadMethods() :
                (execution(* java.net.http.HttpResponse$BodySubscribers.ofFile(..)) ||
                        execution(* java.net.http.HttpResponse$BodySubscribers.ofPublisher(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousSocketChannel write methods
        pointcut asynchronousSocketChannelWriteMethods() :
                execution(* java.nio.channels.AsynchronousSocketChannel.write(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousSocketChannel read methods
        pointcut asynchronousSocketChannelReadMethods() :
                execution(* java.nio.channels.AsynchronousSocketChannel.read(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileChannel write methods
        pointcut fileChannelWriteMethods() :
                execution(* java.nio.channels.FileChannel.write(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileChannel read methods
        pointcut fileChannelReadMethods() :
                execution(* java.nio.channels.FileChannel.read(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SelectableChannel register methods
        pointcut selectableChannelRegisterMethods() :
                execution(* java.nio.channels.SelectableChannel.register(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServerSocketChannel bind methods
        pointcut serverSocketChannelBindMethods() :
                execution(* java.nio.channels.ServerSocketChannel.bind(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServerSocketChannel setOption methods
        pointcut serverSocketChannelSetOptionMethods() :
                execution(* java.nio.channels.ServerSocketChannel.setOption(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SocketChannel bind methods
        pointcut socketChannelBindMethods() :
                execution(* java.nio.channels.SocketChannel.bind(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SocketChannel open methods
        pointcut socketChannelOpenMethods() :
                execution(* java.nio.channels.SocketChannel.open(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SocketChannel read methods
        pointcut socketChannelReadMethods() :
                execution(* java.nio.channels.SocketChannel.read(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SocketChannel setOption methods
        pointcut socketChannelSetOptionMethods() :
                execution(* java.nio.channels.SocketChannel.setOption(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SocketChannel write methods
        pointcut socketChannelWriteMethods() :
                execution(* java.nio.channels.SocketChannel.write(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AccessDeniedException constructors
        pointcut accessDeniedExceptionInitMethods() :
                execution(* java.nio.file.AccessDeniedException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AtomicMoveNotSupportedException constructors
        pointcut atomicMoveNotSupportedExceptionInitMethods() :
                execution(* java.nio.file.AtomicMoveNotSupportedException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DirectoryNotEmptyException constructors
        pointcut directoryNotEmptyExceptionInitMethods() :
                execution(* java.nio.file.DirectoryNotEmptyException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileAlreadyExistsException constructors
        pointcut fileAlreadyExistsExceptionInitMethods() :
                execution(* java.nio.file.FileAlreadyExistsException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystemException constructors
        pointcut fileSystemExceptionInitMethods() :
                execution(* java.nio.file.FileSystemException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystemLoopException constructors
        pointcut fileSystemLoopExceptionInitMethods() :
                execution(* java.nio.file.FileSystemLoopException.<init>(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystems getFileSystem methods
        pointcut fileSystemsGetFileSystemMethods() :
                execution(* java.nio.file.FileSystems.getFileSystem(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystems newFileSystem methods
        pointcut fileSystemsNewFileSystemMethods() :
                execution(* java.nio.file.FileSystems.newFileSystem(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files copy methods
        pointcut filesCopyMethods() :
                execution(* java.nio.file.Files.copy(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files create methods
        pointcut filesCreateMethods() :
                execution(* java.nio.file.Files.create*(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files delete methods
        pointcut filesDeleteMethods() :
                execution(* java.nio.file.Files.delete(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files deleteIfExists methods
        pointcut filesDeleteIfExistsMethods() :
                execution(* java.nio.file.Files.deleteIfExists(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files exists methods
        pointcut filesExistsMethods() :
                execution(* java.nio.file.Files.exists(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files find methods
        pointcut filesFindMethods() :
                execution(* java.nio.file.Files.find(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files getAttribute methods
        pointcut filesGetAttributeMethods() :
                execution(* java.nio.file.Files.getAttribute(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files getFileStore methods
        pointcut filesGetFileStoreMethods() :
                execution(* java.nio.file.Files.getFileStore(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files getLastModifiedTime methods
        pointcut filesGetLastModifiedTimeMethods() :
                execution(* java.nio.file.Files.getLastModifiedTime(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files getPosixFilePermissions methods
        pointcut filesGetPosixFilePermissionsMethods() :
                execution(* java.nio.file.Files.getPosixFilePermissions(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isDirectory methods
        pointcut filesIsDirectoryMethods() :
                execution(* java.nio.file.Files.isDirectory(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isExecutable methods
        pointcut filesIsExecutableMethods() :
                execution(* java.nio.file.Files.isExecutable(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isReadable methods
        pointcut filesIsReadableMethods() :
                execution(* java.nio.file.Files.isReadable(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isRegularFile methods
        pointcut filesIsRegularFileMethods() :
                execution(* java.nio.file.Files.isRegularFile(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isSameFile methods
        pointcut filesIsSameFileMethods() :
                execution(* java.nio.file.Files.isSameFile(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isSymbolicLink methods
        pointcut filesIsSymbolicLinkMethods() :
                execution(* java.nio.file.Files.isSymbolicLink(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files isWritable methods
        pointcut filesIsWritableMethods() :
                execution(* java.nio.file.Files.isWritable(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files lines methods
        pointcut filesLinesMethods() :
                execution(* java.nio.file.Files.lines(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files list methods
        pointcut filesListMethods() :
                execution(* java.nio.file.Files.list(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files mismatch methods
        pointcut filesMismatchMethods() :
                execution(* java.nio.file.Files.mismatch(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files move methods
        pointcut filesMoveMethods() :
                execution(* java.nio.file.Files.move(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newBufferedReader methods
        pointcut filesNewBufferedReaderMethods() :
                execution(* java.nio.file.Files.newBufferedReader(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newBufferedWriter methods
        pointcut filesNewBufferedWriterMethods() :
                execution(* java.nio.file.Files.newBufferedWriter(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newByteChannel methods
        pointcut filesNewByteChannelMethods() :
                execution(* java.nio.file.Files.newByteChannel(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newDirectoryStream methods
        pointcut filesNewDirectoryStreamMethods() :
                execution(* java.nio.file.Files.newDirectoryStream(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newInputStream methods
        pointcut filesNewInputStreamMethods() :
                execution(* java.nio.file.Files.newInputStream(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files newOutputStream methods
        pointcut filesNewOutputStreamMethods() :
                execution(* java.nio.file.Files.newOutputStream(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files notExists methods
        pointcut filesNotExistsMethods() :
                execution(* java.nio.file.Files.notExists(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files probeContentType methods
        pointcut filesProbeContentTypeMethods() :
                execution(* java.nio.file.Files.probeContentType(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files readAllBytes methods
        pointcut filesReadAllBytesMethods() :
                execution(* java.nio.file.Files.readAllBytes(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files readAllLines methods
        pointcut filesReadAllLinesMethods() :
                execution(* java.nio.file.Files.readAllLines(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files readAttributes methods
        pointcut filesReadAttributesMethods() :
                execution(* java.nio.file.Files.readAttributes(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files readString methods
        pointcut filesReadStringMethods() :
                execution(* java.nio.file.Files.readString(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files readSymbolicLink methods
        pointcut filesReadSymbolicLinkMethods() :
                execution(* java.nio.file.Files.readSymbolicLink(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files setAttribute methods
        pointcut filesSetAttributeMethods() :
                execution(* java.nio.file.Files.setAttribute(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files size methods
        pointcut filesSizeMethods() :
                execution(* java.nio.file.Files.size(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files walk methods
        pointcut filesWalkMethods() :
                execution(* java.nio.file.Files.walk(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files walkFileTree methods
        pointcut filesWalkFileTreeMethods() :
                execution(* java.nio.file.Files.walkFileTree(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files write methods
        pointcut filesWriteMethods() :
                execution(* java.nio.file.Files.write(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.Files writeString methods
        pointcut filesWriteStringMethods() :
                execution(* java.nio.file.Files.writeString(..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for java.nio.file.LinkPermission constructors
        pointcut linkPermissionMethods() :
                execution(java.nio.file.LinkPermission.new(String)) ||
                        execution(java.nio.file.LinkPermission.new(String, String));

        // Pointcut for java.nio.file.NoSuchFileException constructors
        pointcut noSuchFileExceptionMethods() :
                execution(java.nio.file.NoSuchFileException.new(String)) ||
                        execution(java.nio.file.NoSuchFileException.new(String, String, String));

        // Pointcut for java.nio.file.NotDirectoryException constructors
        pointcut notDirectoryExceptionMethods() :
                execution(java.nio.file.NotDirectoryException.new(String));

        // Pointcut for java.nio.file.NotLinkException constructors
        pointcut notLinkExceptionMethods() :
                execution(java.nio.file.NotLinkException.new(String)) ||
                        execution(java.nio.file.NotLinkException.new(String, String, String));

        // Pointcut for java.nio.file.Path methods
        pointcut pathMethods() :
                execution(* java.nio.file.Path.endsWith(String)) ||
                        execution(* java.nio.file.Path.of(String, String[])) ||
                        execution(* java.nio.file.Path.of(java.net.URI)) ||
                        execution(* java.nio.file.Path.resolve(String)) ||
                        execution(* java.nio.file.Path.resolveSibling(String)) ||
                        execution(* java.nio.file.Path.startsWith(String));

        // Pointcut for java.nio.file.Paths methods
        pointcut pathsMethods() :
                execution(* java.nio.file.Paths.get(String, String[])) ||
                        execution(* java.nio.file.Paths.get(java.net.URI));

        // Pointcut for java.nio.file.attribute.UserPrincipalNotFoundException constructor
        pointcut userPrincipalNotFoundExceptionMethods() :
                execution(java.nio.file.attribute.UserPrincipalNotFoundException.new(String));

        // Pointcut for java.nio.file.spi.FileSystemProvider methods
        pointcut fileSystemProviderMethods() :
                execution(* java.nio.file.spi.FileSystemProvider.deleteIfExists(java.nio.file.Path)) ||
                        execution(* java.nio.file.spi.FileSystemProvider.exists(java.nio.file.Path, java.nio.file.LinkOption[])) ||
                        execution(* java.nio.file.spi.FileSystemProvider.newInputStream(java.nio.file.Path, java.nio.file.OpenOption[])) ||
                        execution(* java.nio.file.spi.FileSystemProvider.newOutputStream(java.nio.file.Path, java.nio.file.OpenOption[])) ||
                        execution(* java.nio.file.spi.FileSystemProvider.readAttributesIfExists(java.nio.file.Path, Class, java.nio.file.LinkOption[]));

}