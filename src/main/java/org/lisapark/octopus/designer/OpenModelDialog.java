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
package org.lisapark.octopus.designer;

import com.google.common.collect.Lists;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
import org.lisapark.octopus.core.ProcessingModel;
import org.lisapark.octopus.repository.OctopusRepository;
import org.lisapark.octopus.repository.RepositoryException;
import org.lisapark.octopus.swing.ComponentFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * @author dave sinclair(david.sinclair@lisa-park.com)
 * Modified by Alex Mylnikov (alexmy@lisa-park.com) 2013-11-14
 */
public class OpenModelDialog extends StandardDialog {

    private final SearchResultListModel searchResultsModel = new SearchResultListModel();
    
    private static OctopusRepository repository;
    private ProcessingModel selectedProcessingModel;
    private JButton okButton;
    
    private final String jurl;
    
    private static String turl;
    private static Integer tport;
    private static String tuid;
    private static String tpsw;
    
    private static JCheckBox searchOnServerChk;

    private OpenModelDialog(JFrame frame, OctopusRepository repository, String jurl,
                        String turl, Integer tport, String tuid, String tpsw) {
        super(frame, "Octopus");
        setResizable(false);
        OpenModelDialog.repository = repository;
        
        this.jurl = jurl;
        OpenModelDialog.turl = turl;
        OpenModelDialog.tport = tport;
        OpenModelDialog.tuid = tuid;
        OpenModelDialog.tpsw = tpsw;
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel("Open model",
                "Please enter the name of the model that you want to open."
                + " Attention. Do not use spcial symbols.",
                DesignerIconsFactory.getImageIcon(DesignerIconsFactory.OCTOPUS_LARGE));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        JLabel modelNameLbl = ComponentFactory.createLabelWithTextAndMnemonic("Model name: ", KeyEvent.VK_N);
        modelNameLbl.setHorizontalAlignment(SwingConstants.CENTER);

        final JTextField modelNameTxt = ComponentFactory.createTextField();
        modelNameTxt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchForModels(modelNameTxt.getText());
            }
        });
        modelNameLbl.setLabelFor(modelNameTxt);

        JButton searchBtn = ComponentFactory.createButtonWithAction(new AbstractAction("Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchForModels(modelNameTxt.getText());
            }
        });
        searchBtn.setMnemonic(KeyEvent.VK_S);

        // note that this padding numbers are Jide recommendations
        JPanel topPanel = ComponentFactory.createPanelWithLayout(new BorderLayout(6, 6));
        
        searchOnServerChk = new JCheckBox("Search on Server Repository");
        topPanel.add(searchOnServerChk, BorderLayout.CENTER);
        
        JPanel searchPanel = ComponentFactory.createPanelWithLayout(new BorderLayout());
        
        searchPanel.add(modelNameLbl, BorderLayout.BEFORE_LINE_BEGINS);        
        searchPanel.add(modelNameTxt, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.AFTER_LINE_ENDS);
        
        topPanel.add(searchPanel, BorderLayout.SOUTH);        

        // note that this padding numbers are Jide recommendations
        JPanel contentPanel = ComponentFactory.createPanelWithLayout(new BorderLayout(10, 10));
        // note that this padding numbers are Jide recommendations
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        contentPanel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        final JList searchList = ComponentFactory.createListWithModel(searchResultsModel);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setCellRenderer(new SearchResultListCellRenderer());
        
        searchList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {

                    int selectedIndex = searchList.getSelectedIndex();
                    if (selectedIndex > -1) {
                        okButton.setEnabled(true);
                        selectedProcessingModel = (ProcessingModel) searchResultsModel.getModelAt(selectedIndex);
                    } else {
                        okButton.setEnabled(false);
                    }
                }
            }
        });

        JScrollPane scrollPane = ComponentFactory.createScrollPaneWithComponent(searchList);
        scrollPane.setPreferredSize(new Dimension(400, 250));

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // we want the model name text field to have focus first when the dialog opens
        setInitFocusedComponent(modelNameTxt);

        return contentPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        // note that these padding numbers coincide with what Jide recommends for StandardDialog button panels
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        okButton = ComponentFactory.createButton();
        okButton.setName(OK);
        okButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        });
        
        // we need to disable the button AFTER setting the action
        okButton.setEnabled(false);

        JButton cancelButton = ComponentFactory.createButton();
        cancelButton.setName(CANCEL);
        cancelButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.cancelButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });

        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);

        return buttonPanel;
    }

    /**
     * Uses the {@link #repository} to search for processing models that are named like the specified searchCriteria.
     *
     * @param searchCriteria to use when searching
     */
    private synchronized void searchForModels(String searchCriteria) {
        List<String> models;
        try {           
            if(searchOnServerChk.isSelected()){
                models = repository.getModelNameList(searchCriteria, jurl);
            } else {
                models = getModelList(repository.getProcessingModelsByName(searchCriteria));                
            }
            searchResultsModel.setProcessingModels(models);
        } catch (RepositoryException e) {
            ErrorDialog.showErrorDialog("Octopus", this, e, "Problem searching for models.");
        }
    }

    private List<String> getModelList(List<ProcessingModel> processingModelsByName) {
        List<String> modelNameList = Lists.newArrayList();
        for(ProcessingModel model : processingModelsByName){
            modelNameList.add(model.getModelName());
        }
        return modelNameList;
    }

    private static class SearchResultListModel extends AbstractListModel {

        private java.util.List<String> modelNameList;

        private void setProcessingModels(List<String> modelNameList) {
            this.modelNameList = modelNameList;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public int getSize() {
            return modelNameList != null ? modelNameList.size() : 0;
        }

        @Override
        public String getElementAt(int index) {
            return modelNameList.get(index);
        }
        
        public ProcessingModel getModelAt(int index) { 
            try {
                if (searchOnServerChk.isSelected()) {
                    return getRemoteProcessingModel(modelNameList.get(index));
                } else {
                    return repository.getProcessingModelByName(modelNameList.get(index));
                }
            } catch (RepositoryException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }            
        }

        private ProcessingModel getRemoteProcessingModel(String modelName) throws RepositoryException {
            return repository.getProcessingModelByName(modelName, turl, tport, tuid, tpsw);
        }
    }

    /**
     * {@link ListCellRenderer} for displaying {@link ProcessingModel}s
     */
    private static class SearchResultListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String modelName = (String) value;
            return super.getListCellRendererComponent(list, modelName, index, isSelected, cellHasFocus);
        }
    }

    /**
     * This method will create and display a new {@link org.lisapark.octopus.designer.OpenModelDialog} for opening
     * a {@link ProcessingModel}.
     *
     * @param parent     to center dialog over
     * @param repository used for searching for models
     * @param rurl
     * @param rport
     * @param ruid
     * @param rpsw
     * @return selected model, or null if the user canceled
     */
    public static ProcessingModel openProcessingModel(Component parent, OctopusRepository repository, String jurl,
                        String rurl, Integer rport, String ruid, String rpsw) {
        
        JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parent);
        OpenModelDialog dialog = new OpenModelDialog(frame, repository, jurl, rurl, rport, ruid, rpsw);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        if (dialog.getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
            return dialog.selectedProcessingModel;
        } else {
            return null;
        }
    }
}

