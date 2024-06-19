/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.hibernate.SCDItemMetadataDao;
import org.akaza.openclinica.domain.crfdata.SCDItemMetadataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

/**
 * Service handling the simple conditional display logic
 * @author ywang
 */
public class SimpleConditionalDisplayService {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private DataSource dataSource;
    private SCDItemMetadataDao scdItemMetadataDao;

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SCDItemMetadataDao getScdItemMetadataDao() {
        return this.scdItemMetadataDao;
    }

    public void setScdItemMetadataDao(SCDItemMetadataDao scdItemMetadataDao) {
        this.scdItemMetadataDao = scdItemMetadataDao;
    }

    /**
     * SimpleConditionalDisplayService get created by Spring DI
     * @param dataSource DataSource
     */
    public SimpleConditionalDisplayService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Initialize ItemFormMetadataBean in DisplaySectionBean for simple conditional display
     * @param displaySection DisplaySectionBean to initialise
     * @return initialised DisplaySectionBean
     */
    public DisplaySectionBean initConditionalDisplays(DisplaySectionBean displaySection) {
        int sectionId = displaySection.getSection().getId();
        Set<Integer> showSCDItemIds = displaySection.getShowSCDItemIds();
        ArrayList<SCDItemMetadataBean> cds = scdItemMetadataDao.findAllBySectionId(sectionId);
        if (cds == null) {
            logger.info("SCDItemMetadataDao.findAllBySectionId with sectionId=" + sectionId + " returned null.");
        } else if (!cds.isEmpty()){
            ArrayList<DisplayItemBean> displayItems = initSCDItems(displaySection.getItems(), cds);
            HashMap<Integer, ArrayList<SCDItemMetadataBean>> scdPairMap = getControlMetaIdAndSCDSetMap(cds);
            if (scdPairMap == null) {
                logger.info("SimpleConditionalDisplayService.getControlMetaIdAndSCDSetMap returned null.");
            } else {
                for (DisplayItemBean displayItem : displayItems) {
                    if (scdPairMap.containsKey(displayItem.getMetadata().getId())) {
                        //displayItem is control item
                        ArrayList<SCDItemMetadataBean> sets = scdPairMap.get(displayItem.getMetadata().getId());
                        displayItem.getScdData().setScdSetsForControl(sets);
                        for (SCDItemMetadataBean scd : sets) {
                            if (SimpleConditionalDisplayService.initConditionalDisplayToBeShown(displayItem, scd)) {
                                showSCDItemIds.add(scd.getScdItemId());
                            }
                        }
                    }
                    //control item is ahead of its scd item(s)
                    if (displayItem.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0) {
                        //displayItem is scd item
                        displayItem.setIsSCDtoBeShown(showSCDItemIds.contains(displayItem.getMetadata().getItemId()));
                    }
                    
                    if (!displayItem.getChildren().isEmpty()) {
                        ArrayList<DisplayItemBean> cs = displayItem.getChildren();
                        for (DisplayItemBean c : cs) {
                            if (scdPairMap.containsKey(c.getMetadata().getId())) {
                                //c is control item
                                ArrayList<SCDItemMetadataBean> sets = scdPairMap.get(c.getMetadata().getId());
                                c.getScdData().setScdSetsForControl(sets);
                                for (SCDItemMetadataBean scd : sets) {
                                    if (SimpleConditionalDisplayService.initConditionalDisplayToBeShown(c, scd)) {
                                        showSCDItemIds.add(scd.getScdItemId());
                                    }
                                }
                            }
                            //control item is ahead of its scd item(s)
                            if (c.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0) {
                                //c is scd item
                                c.setIsSCDtoBeShown(showSCDItemIds.contains(c.getMetadata().getItemId()));
                            }
                        }
                    }
                }
            }
        }
        return displaySection;
    }
    
    public ArrayList<DisplayItemBean> initSCDItems(ArrayList<DisplayItemBean> displayItems, ArrayList<SCDItemMetadataBean> cds) {
        HashMap<Integer, SCDItemMetadataBean> scds = (HashMap<Integer, SCDItemMetadataBean>)this.getSCDMetaIdAndSCDSetMap(cds);
        for (DisplayItemBean displayItem : displayItems) {
            ItemFormMetadataBean meta = displayItem.getMetadata();
            if (scds.containsKey(meta.getId())) {
                SCDItemMetadataBean scdItemMetadataBean = scds.get(meta.getId());
                scdItemMetadataBean.setScdItemId(meta.getItemId());
                displayItem.getScdData().setScdItemMetadataBean(scdItemMetadataBean);
            }
            if (meta.getParentId() < 1) {
                ArrayList<DisplayItemBean> cs = displayItem.getChildren();
                for (DisplayItemBean c : cs) {
                    ItemFormMetadataBean cmeta = c.getMetadata();
                    if (scds.containsKey(cmeta.getId())) {
                        SCDItemMetadataBean scdItemMetadataBean = scds.get(cmeta.getId());
                        scdItemMetadataBean.setScdItemId(cmeta.getItemId());
                        c.getScdData().setScdItemMetadataBean(scdItemMetadataBean);
                    }
                }
            }
        }
        return displayItems;
    }
    
    public HashMap<Integer, ArrayList<SCDItemMetadataBean>> getControlMetaIdAndSCDSetMap(ArrayList<SCDItemMetadataBean> scdSets) {
        HashMap<Integer, ArrayList<SCDItemMetadataBean>> cdPairMap = new HashMap<>();
        if (scdSets == null) {
            logger.info("SimpleConditionalDisplayService.getControlMetaIdAndSCDSetMap() ArrayList<SCDItemMetadataBean> parameter is null.");
        } else {
            for(SCDItemMetadataBean scd : scdSets) {
                Integer conId = scd.getControlItemFormMetadataId();
                ArrayList<SCDItemMetadataBean> conScds = cdPairMap.containsKey(conId) ? cdPairMap.get(conId) : new ArrayList<>();
                conScds.add(scd);
                cdPairMap.put(conId, conScds);
            }
        }
        return cdPairMap;
    }
    
    public Map<Integer,SCDItemMetadataBean> getSCDMetaIdAndSCDSetMap(ArrayList<SCDItemMetadataBean> scdSets) {
        Map<Integer,SCDItemMetadataBean> map = new HashMap<>();
        if (scdSets == null) {
            logger.info("SimpleConditionalDisplayService.getSCDMetaIdAndSCDSetMap() ArrayList<SCDItemMetadataBean> parameter is null.");
        } else {
            for (SCDItemMetadataBean scd : scdSets) {
                map.put(scd.getScdItemFormMetadataId(), scd);
            }
        }
        return map;
    }
    
    /**
     * Base on chosen option of a control item. scdItemId has to be initialized for SCDItemMetadataBean.
     * @param dib DisplayItemBean
     * @param showSCDItemIds Set of SCD item IDs
     * @return Set of SCD item IDs
     */
    public static Set<Integer> conditionalDisplayToBeShown(DisplayItemBean dib, Set<Integer> showSCDItemIds) {
        // Conditional display item will be always after its control item
        ArrayList<SCDItemMetadataBean> cds = dib.getScdData().getScdSetsForControl();
        if (!cds.isEmpty()) {
            for(SCDItemMetadataBean cd : cds) {
                Integer scdItemId = cd.getScdItemId();
                if (scdItemId > 0) {
                    // If it should be shown add it the item ID to the list
                    if (conditionalDisplayToBeShown(dib.getData().getValue(), cd)) {
                        showSCDItemIds.add(scdItemId);
                    } else { // Otherwise it should be removed from the list if the ID is there
                        showSCDItemIds.remove(scdItemId);
                    }
                }
            }
        }
        return showSCDItemIds;
    }
 
    /**
     * Return true if a SCDItemMetadataBean has a chosen optionValue
     * @param chosenOption SCD controlling item chosen option string
     * @param cd SCDItemMetadataBean of conditional display item
     * @return true if a SCDItemMetadataBean has a chosen optionValue
     */
    public static boolean conditionalDisplayToBeShown(String chosenOption, SCDItemMetadataBean cd) {
        if (chosenOption != null && !chosenOption.isEmpty()) {
            chosenOption = chosenOption.replaceAll("\\\\,", "##");
            if (chosenOption.contains(",")) {
                String[] ss = chosenOption.split(",");
                for (String s : ss) {
                    if (s.replaceAll("##", "\\\\,").trim().equalsIgnoreCase(cd.getOptionValue())) {
                        return true;
                    }
                }
            } else {
                //TODO: commented this out because it has no effect anyway, candidate for removal or fix
                //chosenOption.replaceAll("##", "\\,");
                return chosenOption.equalsIgnoreCase(cd.getOptionValue());
            }
        }
        
        return false;
    }

    /**
     * Return true if a SCDItemMetadataBean to show initially.
     * @param controlItem DisplayItemBean of item controlling the SCD show/hide logic
     * @param cd SCDItemMetadataBean of conditional display item
     * @return true if a SCDItemMetadataBean to show initially
     */
    public static boolean initConditionalDisplayToBeShown(DisplayItemBean controlItem, SCDItemMetadataBean cd) {
        String chosenOption = controlItem.getData().getValue();
        
        // If there is data assigned to control item
        if (chosenOption != null && !chosenOption.isEmpty()) {

            // If control item is checkbox or multiselect
            // Control item is of different response type
            if (controlItem.getMetadata().getResponseSet().getResponseTypeId() == 3 ||
                controlItem.getMetadata().getResponseSet().getResponseTypeId() == 7) {

                // Mask comma that would interfere with multiselect separator
                chosenOption = chosenOption.replaceAll("\\\\,", "##");
                // If there are multiple options selected
                if (chosenOption.contains(",")) {
                    String[] ss = chosenOption.split(",");
                    for (String s : ss) {
                        // Unmask comma and compare
                        if (s.replaceAll("##", "\\\\,").trim().equalsIgnoreCase(cd.getOptionValue())) {
                            return true;
                        }
                    }
                } else { // Only one option from multiselect is selected
                    return chosenOption.replaceAll("##", "\\\\,").trim().equalsIgnoreCase(cd.getOptionValue());
                }
            } else {
                return chosenOption.equals(cd.getOptionValue());
            }
        } else { // When no data assigned to control item, the default value of item need to be checked
            chosenOption = controlItem.getMetadata().getDefaultValue();
            if (chosenOption != null && !chosenOption.isEmpty()) {
                return chosenOption.equals(cd.getOptionValue());
            } else {
                // For single-select the default is first option form the list
                if (controlItem.getMetadata().getResponseSet().getResponseTypeId() == 6) {
                    chosenOption = (controlItem.getMetadata().getResponseSet().getOptions().get(0)).getValue();
                    if (chosenOption != null && !chosenOption.isEmpty()) {
                        return chosenOption.equals(cd.getOptionValue());
                    }
                }
            }
        }
        
        return false;
    }
    
}
