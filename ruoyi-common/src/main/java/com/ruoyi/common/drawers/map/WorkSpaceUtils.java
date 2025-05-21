package com.ruoyi.common.drawers.map;

import com.supermap.data.*;

/**
 * @author: xiaodemos
 * @date: 2025-03-06 11:02
 * @description: 工作空间工具类
 */

public class WorkSpaceUtils {


    /**
     * @param workspacePath 工作空间路径
     * @author: xiaodemos
     * @date: 2025/3/6 11:36
     * @description: 打开工作空间
     */
    public static Workspace open(String workspacePath) {
        if (workspacePath == null) {
            return null;
        }

        Workspace workspace = new Workspace();
        WorkspaceConnectionInfo workspaceConnectionInfo = new WorkspaceConnectionInfo();
        // 设置工作空间类型
        workspaceConnectionInfo.setType(WorkspaceType.SMWU);

        // 打开工作空间
        workspaceConnectionInfo.setServer(workspacePath);
        workspace.open(workspaceConnectionInfo);

        if (!workspace.open(workspaceConnectionInfo)) {
            return null;
        }

        return workspace;
    }

    /**
     * @param workspace 工作空间
     * @param datasourceName 数据源名称
     * @param datasetName 数据集名称
     * @author: xiaodemos
     * @date: 2025/3/6 11:36
     * @description: 根据工作空间获取数据集
     */
    public static DatasetVector getDatasource(Workspace workspace, String datasourceName, String datasetName) {
        if (workspace == null || datasetName == null) {
            return null;
        }

        Datasource datasource = workspace.getDatasources().get(datasourceName);
        DatasetVector vector = (DatasetVector) datasource.getDatasets().get(datasetName);

        return vector;
    }

    /**
     * @param workspace 工作空间
     * @param workspaceConnectionInfo 工作空间连接信息
     * @param dataset 数据集名称
     * @param savePath 另存为路径
     * @author: xiaodemos
     * @date: 2025/3/6 11:36
     * @description: 保存当前工作空间并关闭所有资源
     */
    public static Workspace saveWithClose(Workspace workspace,
                                          WorkspaceConnectionInfo workspaceConnectionInfo,
                                          DatasetVector dataset,
                                          String savePath) {

        WorkspaceConnectionInfo workspaceConnectionInfoSaveAs = null;

        try {
            // 保存工作空间
            workspace.save();

            // 另存工作空间
            workspaceConnectionInfoSaveAs = new WorkspaceConnectionInfo(savePath);

            if (workspace.saveAs(workspaceConnectionInfoSaveAs)) {
                System.out.println("另存工作空间成功！");
                return workspace;
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            // 释放资源
            dataset.close();
            workspaceConnectionInfo.dispose();
            workspaceConnectionInfoSaveAs.dispose();
            workspace.close();
            workspace.dispose();
        }

        return workspace;
    }

}
