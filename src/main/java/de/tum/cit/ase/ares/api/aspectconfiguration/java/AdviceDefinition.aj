package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.JoinPoint;

public aspect AdviceDefinition {

    private boolean handleAroundAdvice(JoinPoint thisJoinPoint, String operationType) {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && isOperationAllowed(interaction, operationType));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    private boolean isOperationAllowed(FileSystemInteraction interaction, String operationType) {
        switch (operationType.toLowerCase()) {
            case "read":
                return interaction.studentsAreAllowedToReadAllFiles();
            case "write":
                return interaction.studentsAreAllowedToOverwriteAllFiles();
            case "execute":
                return interaction.studentsAreAllowedToExecuteAllFiles();
            case "delete":
                return interaction.studentsAreAllowedToDeleteAllFiles();
            default:
                throw new IllegalArgumentException("Invalid operation type: " + operationType);
        }
    }

    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    Object around() : PointcutDefinitions.appletMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read") ||
                handleAroundAdvice(thisJoinPoint, "write") ||
                handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.awtEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.awtExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.awtPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.basicStrokeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.borderLayoutMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.buttonMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.canvasMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.cardLayoutMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.checkboxMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.checkboxMenuItemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.choiceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.componentReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.componentWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.componentExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.componentOrientationMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.containerReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.containerWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.containerExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.containerOrderFocusTraversalPolicyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.cursorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.defaultKeyboardFocusManagerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.desktopReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.desktopWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.desktopExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dialogMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute") ||
                handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.eventQueueMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileDialogConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileDialogWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileDialogExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fontConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fontReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fontWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.flowLayoutMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.focusTraversalPolicyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fontMetricsReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.frameConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.frameWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.frameExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.gradientPaintMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.graphicsConfigurationWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.graphicsDeviceReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.graphicsDeviceWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.graphicsEnvironmentReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.layoutContainerWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.imageWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.imageReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.keyboardFocusManagerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.keyboardFocusManagerWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.keyboardFocusManagerExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.labelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.labelAddNotifyMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.linearGradientPaintCreateContextMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.listConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.listAddNotifyMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.mediaTrackerReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuBarConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuBarWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuComponentConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuComponentWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuItemConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.menuItemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.mouseInfoReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.panelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.panelAddNotifyMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.popupMenuConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.popupMenuWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.radialGradientPaintCreateContextMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.renderingHintsCloneMethod() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.robotConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.robotWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.scrollPaneConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.scrollPaneWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.scrollPaneAdjustableWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.scrollbarConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.scrollbarWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.splashScreenWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.splashScreenReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.systemTrayWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.systemTrayReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.taskbarReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textAreaConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textAreaAddNotifyMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textComponentWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textFieldConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textFieldAddNotifyMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.texturePaintWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.toolkitWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.toolkitReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.trayIconConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.trayIconReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.windowConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.windowWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.windowReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.iccProfileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.iccProfileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.clipboardWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.clipboardReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dataFlavorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) { // Assuming most DataFlavor methods are read operations
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.mimeTypeParseExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) { // Assuming constructors are considered as execute
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.stringSelectionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) { // Assuming getTransferData is a read operation
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.systemFlavorMapMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) { // Assuming most SystemFlavorMap methods are write operations
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.unsupportedFlavorExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) { // Assuming constructors are considered as execute
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.desktopEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dragGestureEventStartDragMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dragGestureRecognizerAddListenerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dragSourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dragSourceContextMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dropTargetMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.actionEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.adjustmentEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.componentEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.containerEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.focusEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.hierarchyEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputMethodEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.invocationEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.itemEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.keyEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.mouseEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.mouseWheelEventMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.paintEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.windowEventConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.glyphVectorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.imageGraphicAttributeConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.lineBreakMeasurerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.shapeGraphicAttributeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textLayoutMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.textMeasurerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.affineTransformMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.areaMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.cubicCurve2DMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.noninvertibleTransformExceptionConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputContextMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.abstractMultiResolutionImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.affineTransformOpMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.baseMultiResolutionImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.colorConvertOpMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.convolveOpMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.lookupOpMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.pixelGrabberConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.rescaleOpMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.volatileImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.printerExceptionConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.printerJobMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.beanDescriptorConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.beansMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.encoderMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.eventHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.eventSetDescriptorConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.eventSetDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.expressionConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.expressionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.featureDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.indexedPropertyDescriptorConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.indexedPropertyDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.introspectionExceptionConstructor() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.introspectorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.methodDescriptorConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.methodDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.persistenceDelegateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.propertyChangeSupportMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.propertyDescriptorConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.propertyDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.propertyVetoExceptionConstructor() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.simpleBeanInfoMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.statementConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.statementExecuteMethod() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.xmlDecoderConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.xmlDecoderMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.xmlEncoderConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.xmlEncoderMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.beanContextChildSupportMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.beanContextServicesSupportMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read") || handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.beanContextSupportMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedInputStreamCloseMethod() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedOutputStreamConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedReaderCloseMethod() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.bufferedWriterCloseMethod() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.byteArrayOutputStreamConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.byteArrayOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.charArrayReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.charArrayWriterWriteToMethod() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dataInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dataOutputStreamConstructors() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.dataOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileConversionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileInputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileOutputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.fileWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterInputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterOutputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterReaderResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.filterWriterResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputStreamReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.inputStreamReaderResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.lineNumberInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.lineNumberReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.lineNumberReaderResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.objectOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.objectOutputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.outputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.outputStreamResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.pipedInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.pipedOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.pipedReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    Object around() : PointcutDefinitions.pipedWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PipedInputStream read methods
    Object around() : PointcutDefinitions.pipedInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PipedOutputStream write methods
    Object around() : PointcutDefinitions.pipedOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PipedReader read methods
    Object around() : PointcutDefinitions.pipedReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PipedWriter write methods
    Object around() : PointcutDefinitions.pipedWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PrintStream write methods
    Object around() : PointcutDefinitions.printStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PrintWriter write methods
    Object around() : PointcutDefinitions.printWriterWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PushbackInputStream read methods
    Object around() : PointcutDefinitions.pushbackInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for PushbackReader read methods
    Object around() : PointcutDefinitions.pushbackReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for RandomAccessFile read methods
    Object around() : PointcutDefinitions.randomAccessFileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for RandomAccessFile write methods
    Object around() : PointcutDefinitions.randomAccessFileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Reader read methods
    Object around() : PointcutDefinitions.readerReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SequenceInputStream read methods
    Object around() : PointcutDefinitions.sequenceInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for StringReader read methods
    Object around() : PointcutDefinitions.stringReaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Writer write methods
    Object around() : PointcutDefinitions.writerWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Authenticator read methods
    Object around() : PointcutDefinitions.authenticatorReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Authenticator write methods
    Object around() : PointcutDefinitions.authenticatorWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for DatagramSocket initialization methods
    Object around() : PointcutDefinitions.datagramSocketInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for DatagramSocket write methods
    Object around() : PointcutDefinitions.datagramSocketWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for HttpURLConnection read methods
    Object around() : PointcutDefinitions.httpURLConnectionReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for HttpURLConnection write methods
    Object around() : PointcutDefinitions.httpURLConnectionWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for IDN read methods
    Object around() : PointcutDefinitions.idnReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for InetAddress read methods
    Object around() : PointcutDefinitions.inetAddressReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for JarURLConnection read methods
    Object around() : PointcutDefinitions.jarURLConnectionReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for MulticastSocket read methods
    Object around() : PointcutDefinitions.multicastSocketReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for MulticastSocket write methods
    Object around() : PointcutDefinitions.multicastSocketWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for ServerSocket read methods
    Object around() : PointcutDefinitions.serverSocketReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for ServerSocket write methods
    Object around() : PointcutDefinitions.serverSocketWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Socket read methods
    Object around() : PointcutDefinitions.socketReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for Socket write methods
    Object around() : PointcutDefinitions.socketWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for URI read methods
    Object around() : PointcutDefinitions.uriReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for URL read methods
    Object around() : PointcutDefinitions.urlReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for URLClassLoader read methods
    Object around() : PointcutDefinitions.urlClassLoaderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for URLConnection read methods
    Object around() : PointcutDefinitions.urlConnectionReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for HttpRequest$BodyPublishers write methods
    Object around() : PointcutDefinitions.httpRequestBodyPublishersWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for HttpResponse$BodyHandlers read methods
    Object around() : PointcutDefinitions.httpResponseBodyHandlersReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for HttpResponse$BodySubscribers read methods
    Object around() : PointcutDefinitions.httpResponseBodySubscribersReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for AsynchronousSocketChannel write methods
    Object around() : PointcutDefinitions.asynchronousSocketChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for AsynchronousSocketChannel read methods
    Object around() : PointcutDefinitions.asynchronousSocketChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileChannel write methods
    Object around() : PointcutDefinitions.fileChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileChannel read methods
    Object around() : PointcutDefinitions.fileChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SelectableChannel register methods
    Object around() : PointcutDefinitions.selectableChannelRegisterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for ServerSocketChannel bind methods
    Object around() : PointcutDefinitions.serverSocketChannelBindMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for ServerSocketChannel setOption methods
    Object around() : PointcutDefinitions.serverSocketChannelSetOptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SocketChannel bind methods
    Object around() : PointcutDefinitions.socketChannelBindMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SocketChannel open methods
    Object around() : PointcutDefinitions.socketChannelOpenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SocketChannel read methods
    Object around() : PointcutDefinitions.socketChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SocketChannel setOption methods
    Object around() : PointcutDefinitions.socketChannelSetOptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for SocketChannel write methods
    Object around() : PointcutDefinitions.socketChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for AccessDeniedException constructors
    Object around() : PointcutDefinitions.accessDeniedExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for AtomicMoveNotSupportedException constructors
    Object around() : PointcutDefinitions.atomicMoveNotSupportedExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for DirectoryNotEmptyException constructors
    Object around() : PointcutDefinitions.directoryNotEmptyExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileAlreadyExistsException constructors
    Object around() : PointcutDefinitions.fileAlreadyExistsExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileSystemException constructors
    Object around() : PointcutDefinitions.fileSystemExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileSystemLoopException constructors
    Object around() : PointcutDefinitions.fileSystemLoopExceptionInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileSystems getFileSystem methods
    Object around() : PointcutDefinitions.fileSystemsGetFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for FileSystems newFileSystem methods
    Object around() : PointcutDefinitions.fileSystemsNewFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throwSecurityException(thisJoinPoint);
        }
    }

    // Around advice for java.nio.file.Files copy methods
    Object around() : PointcutDefinitions.filesCopyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files create methods
    Object around() : PointcutDefinitions.filesCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files delete methods
    Object around() : PointcutDefinitions.filesDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files deleteIfExists methods
    Object around() : PointcutDefinitions.filesDeleteIfExistsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files exists methods
    Object around() : PointcutDefinitions.filesExistsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files find methods
    Object around() : PointcutDefinitions.filesFindMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files getAttribute methods
    Object around() : PointcutDefinitions.filesGetAttributeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files getFileStore methods
    Object around() : PointcutDefinitions.filesGetFileStoreMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files getLastModifiedTime methods
    Object around() : PointcutDefinitions.filesGetLastModifiedTimeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files getPosixFilePermissions methods
    Object around() : PointcutDefinitions.filesGetPosixFilePermissionsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isDirectory methods
    Object around() : PointcutDefinitions.filesIsDirectoryMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isExecutable methods
    Object around() : PointcutDefinitions.filesIsExecutableMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isReadable methods
    Object around() : PointcutDefinitions.filesIsReadableMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isRegularFile methods
    Object around() : PointcutDefinitions.filesIsRegularFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isSameFile methods
    Object around() : PointcutDefinitions.filesIsSameFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isSymbolicLink methods
    Object around() : PointcutDefinitions.filesIsSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files isWritable methods
    Object around() : PointcutDefinitions.filesIsWritableMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files lines methods
    Object around() : PointcutDefinitions.filesLinesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files list methods
    Object around() : PointcutDefinitions.filesListMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files mismatch methods
    Object around() : PointcutDefinitions.filesMismatchMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files move methods
    Object around() : PointcutDefinitions.filesMoveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newBufferedReader methods
    Object around() : PointcutDefinitions.filesNewBufferedReaderMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newBufferedWriter methods
    Object around() : PointcutDefinitions.filesNewBufferedWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newByteChannel methods
    Object around() : PointcutDefinitions.filesNewByteChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newDirectoryStream methods
    Object around() : PointcutDefinitions.filesNewDirectoryStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newInputStream methods
    Object around() : PointcutDefinitions.filesNewInputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files newOutputStream methods
    Object around() : PointcutDefinitions.filesNewOutputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files notExists methods
    Object around() : PointcutDefinitions.filesNotExistsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files probeContentType methods
    Object around() : PointcutDefinitions.filesProbeContentTypeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files readAllBytes methods
    Object around() : PointcutDefinitions.filesReadAllBytesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files readAllLines methods
    Object around() : PointcutDefinitions.filesReadAllLinesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files readAttributes methods
    Object around() : PointcutDefinitions.filesReadAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files readString methods
    Object around() : PointcutDefinitions.filesReadStringMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files readSymbolicLink methods
    Object around() : PointcutDefinitions.filesReadSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files setAttribute methods
    Object around() : PointcutDefinitions.filesSetAttributeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files size methods
    Object around() : PointcutDefinitions.filesSizeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files walk methods
    Object around() : PointcutDefinitions.filesWalkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files walkFileTree methods
    Object around() : PointcutDefinitions.filesWalkFileTreeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files write methods
    Object around() : PointcutDefinitions.filesWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Files writeString methods
    Object around() : PointcutDefinitions.filesWriteStringMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.LinkPermission methods
    Object around() : PointcutDefinitions.linkPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.NoSuchFileException methods
    Object around() : PointcutDefinitions.noSuchFileExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.NotDirectoryException methods
    Object around() : PointcutDefinitions.notDirectoryExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.NotLinkException methods
    Object around() : PointcutDefinitions.notLinkExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Path methods
    Object around() : PointcutDefinitions.pathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.Paths methods
    Object around() : PointcutDefinitions.pathsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.attribute.UserPrincipalNotFoundException methods
    Object around() : PointcutDefinitions.userPrincipalNotFoundExceptionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice for java.nio.file.spi.FileSystemProvider methods
    Object around() : PointcutDefinitions.fileSystemProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

}