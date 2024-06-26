/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;

import static org.akaza.openclinica.core.util.ClassCastHelper.*;

/**
 * Utility class with method for retrieving the metadata for a CRFVersion.
 */
public class CRFVersionMetadataUtil {

	private DataSource dataSource = null;
	
	public CRFVersionMetadataUtil(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	/**
	 * Builds and returns an ArrayList of SectionBeans that comprise the metadata of a CRFVersion.
	 */
    public ArrayList<SectionBean> retrieveFormMetadata(CRFVersionBean version) throws Exception {

        ItemDAO idao = new ItemDAO(dataSource);
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(dataSource);

            // tbh, 102007
            SectionDAO sdao = new SectionDAO(dataSource);
            ItemGroupDAO igdao = new ItemGroupDAO(dataSource);
            ItemGroupMetadataDAO igmdao = new ItemGroupMetadataDAO(dataSource);
            ArrayList<SectionBean> sections = sdao.findByVersionId(version.getId());
            HashMap<Integer, ArrayList<ItemBean>> versionMap = new HashMap<>();
            for (int i = 0; i < sections.size(); i++) {
                SectionBean section = sections.get(i);
                ArrayList<ItemBean> items = asArrayList(section.getItems(), ItemBean.class);
				versionMap.put(new Integer(section.getId()), items);
                // YW 08-21-2007, add group metadata
                ArrayList<ItemGroupBean> igs = (ArrayList<ItemGroupBean>) igdao.findGroupBySectionId(section.getId());
                for (int j = 0; j < igs.size(); ++j) {
                    ArrayList<ItemGroupMetadataBean> igms =
                        (ArrayList<ItemGroupMetadataBean>) igmdao.findMetaByGroupAndSection(igs.get(j).getId(), section.getCRFVersionId(), section.getId());
                    if (!igms.isEmpty()) {
                        // Note, the following logic has been adapted here -
                        // "for a given crf version,
                        // all the items in the same group have the same group
                        // metadata
                        // so we can get one of them and set metadata for the
                        // group"
                        igs.get(j).setMeta(igms.get(0));
                        igs.get(j).setItemGroupMetaBeans(igms);
                    }
                }
                ((SectionBean) sections.get(i)).setGroups(igs);
                // YW >>
            }
            ArrayList<ItemBean> items = idao.findAllItemsByVersionId(version.getId());
            // YW 08-22-2007, if this crf_version_id doesn't exist in
            // item_group_metadata table,
            // items in this crf_version will not exist in item_group_metadata,
            // then different query will be used
            if (igmdao.versionIncluded(version.getId())) {
                for (int i = 0; i < items.size(); i++) {
                    ItemBean item = (ItemBean) items.get(i);
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionId(item.getId(), version.getId());

                    item.setItemMeta(ifm);
                    // logger.info("option******" +
                    // ifm.getResponseSet().getOptions().size());
                    ArrayList<ItemBean> its = versionMap.get(ifm.getSectionId());
                    its.add(item);
                }
            } else {
                for (int i = 0; i < items.size(); i++) {
                    ItemBean item = (ItemBean) items.get(i);
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionIdNotInIGM(item.getId(), version.getId());

                    item.setItemMeta(ifm);
                    ArrayList<ItemBean> its = versionMap.get(ifm.getSectionId());
                    its.add(item);
                }
            }

            for (SectionBean section : sections) {
                section.setItems(versionMap.get(section.getId()));
            }
            return sections;
        }

}
