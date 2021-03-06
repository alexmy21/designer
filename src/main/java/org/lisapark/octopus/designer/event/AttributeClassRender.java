/* 
 * Copyright (c) 2013 Lisa Park, Inc. (www.lisa-park.net).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lisa Park, Inc. (www.lisa-park.net) - initial API and implementation and/or initial documentation
 */
package org.lisapark.octopus.designer.event;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author dave sinclair(david.sinclair@lisa-park.com)
 */
class AttributeClassRender extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null) {
            value = ((Class) value).getSimpleName();
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
