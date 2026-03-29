# 灾害文件管理模块 - 使用示例

## 快速开始

### 1. 数据库准备

首先执行 SQL 脚本创建表：

```bash
# 连接到 PostgreSQL 数据库
psql -U postgres -d your_database

# 执行建表脚本
\i ruoyi-disaster-file/db/disaster_files.sql
```

### 2. 测试数据

插入一些测试数据：

```sql
-- 插入测试文件记录
INSERT INTO disaster_files (
    disaster_id, disaster_type, file_name, file_path, 
    local_file_path, file_size, file_type, file_description, 
    upload_by, create_time, is_deleted
) VALUES 
(
    'RAIN20240101001', 
    'rain', 
    '暴雨评估报告.pdf', 
    '/files/rain/2024/01/report.pdf',
    'D:/data/files/rain/2024/01/report.pdf',
    1024000, 
    'pdf', 
    '2024 年 1 月 1 日暴雨灾害评估报告',
    'admin',
    NOW(),
    0
),
(
    'RAIN20240101001', 
    'rain', 
    '降雨量统计.xlsx', 
    '/files/rain/2024/01/statistics.xlsx',
    'D:/data/files/rain/2024/01/statistics.xlsx',
    512000, 
    'xlsx', 
    '降雨量统计数据',
    'admin',
    NOW(),
    0
),
(
    '123', 
    'earthquake', 
    '地震烈度图.png', 
    '/files/earthquake/2024/01/intensity_map.png',
    'D:/data/files/earthquake/2024/01/intensity_map.png',
    2048000, 
    'png', 
    '地震烈度分布图',
    'admin',
    NOW(),
    0
);
```

### 3. API 调用示例

#### 3.1 查询所有灾害列表

```bash
curl -X GET "http://localhost:8080/disaster/file/disasterList" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "disasterId": "RAIN20240101001",
      "disasterName": "2024 年 1 月 1 日暴雨过程",
      "disasterType": "rain",
      "occurrenceTime": "2024-01-01T10:00:00",
      "position": "某市某区",
      "extraInfo": "100mm"
    },
    {
      "disasterId": "123",
      "disasterName": "某地地震",
      "disasterType": "earthquake",
      "occurrenceTime": "2024-01-02T14:30:00",
      "position": "某县某镇",
      "extraInfo": "5.0 级"
    }
  ]
}
```

#### 3.2 只查询暴雨灾害

```bash
curl -X GET "http://localhost:8080/disaster/file/disasterList?disasterType=rain" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 3.3 只查询地震灾害

```bash
curl -X GET "http://localhost:8080/disaster/file/disasterList?disasterType=earthquake" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 3.4 查询指定灾害的文件列表

```bash
# 查询暴雨 RAIN20240101001 的所有文件
curl -X GET "http://localhost:8080/disaster/file/fileList?disasterId=RAIN20240101001&disasterType=rain" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "fileId": 1,
      "disasterId": "RAIN20240101001",
      "disasterType": "rain",
      "fileName": "暴雨评估报告.pdf",
      "filePath": "/files/rain/2024/01/report.pdf",
      "localFilePath": "D:/data/files/rain/2024/01/report.pdf",
      "fileSize": 1024000,
      "fileType": "pdf",
      "fileDescription": "暴雨灾害评估报告",
      "uploadBy": "admin",
      "createTime": "2024-01-01T12:00:00",
      "updateTime": "2024-01-01T12:00:00",
      "isDeleted": 0
    },
    {
      "fileId": 2,
      "disasterId": "RAIN20240101001",
      "disasterType": "rain",
      "fileName": "降雨量统计.xlsx",
      "filePath": "/files/rain/2024/01/statistics.xlsx",
      "localFilePath": "D:/data/files/rain/2024/01/statistics.xlsx",
      "fileSize": 512000,
      "fileType": "xlsx",
      "fileDescription": "降雨量统计数据",
      "uploadBy": "admin",
      "createTime": "2024-01-01T12:00:00",
      "updateTime": "2024-01-01T12:00:00",
      "isDeleted": 0
    }
  ]
}
```

#### 3.5 删除整场灾害的所有文件

```bash
# 删除暴雨 RAIN20240101001 的所有文件（逻辑删除）
curl -X DELETE "http://localhost:8080/disaster/file/RAIN20240101001/rain" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应：
```json
{
  "code": 200,
  "msg": "删除成功"
}
```

### 4. Java 代码调用示例

```java
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private IDisasterFileService disasterFileService;
    
    /**
     * 测试查询灾害列表
     */
    @GetMapping("/testDisasterList")
    public List<DisasterInfoDTO> testDisasterList() {
        // 查询所有灾害
        List<DisasterInfoDTO> allDisasters = disasterFileService.selectDisasterList(null);
        System.out.println("所有灾害：" + allDisasters);
        
        // 只查询暴雨
        List<DisasterInfoDTO> rainDisasters = disasterFileService.selectDisasterList("rain");
        System.out.println("暴雨灾害：" + rainDisasters);
        
        return allDisasters;
    }
    
    /**
     * 测试查询文件列表
     */
    @GetMapping("/testFileList")
    public List<DisasterFile> testFileList() {
        List<DisasterFile> files = disasterFileService.selectFilesByDisasterId(
            "RAIN20240101001", "rain");
        System.out.println("文件列表：" + files);
        return files;
    }
    
    /**
     * 测试删除文件
     */
    @Log(title = "测试删除文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/testDelete")
    public String testDelete() {
        int result = disasterFileService.deleteFilesByDisasterId(
            "RAIN20240101001", "rain");
        return result > 0 ? "删除成功" : "删除失败";
    }
}
```

### 5. Postman 使用示例

#### 5.1 创建请求集合

1. 创建集合：`灾害文件管理`
2. 添加以下请求：

**请求 1: 查询所有灾害**
- Method: GET
- URL: `{{baseUrl}}/disaster/file/disasterList`
- Headers: `Authorization: Bearer {{token}}`

**请求 2: 查询暴雨灾害**
- Method: GET
- URL: `{{baseUrl}}/disaster/file/disasterList?disasterType=rain`
- Headers: `Authorization: Bearer {{token}}`

**请求 3: 查询文件列表**
- Method: GET
- URL: `{{baseUrl}}/disaster/file/fileList?disasterId=RAIN20240101001&disasterType=rain`
- Headers: `Authorization: Bearer {{token}}`

**请求 4: 删除灾害文件**
- Method: DELETE
- URL: `{{baseUrl}}/disaster/file/RAIN20240101001/rain`
- Headers: `Authorization: Bearer {{token}}`

#### 5.2 环境变量配置

在 Postman 中设置环境变量：
- `baseUrl`: `http://localhost:8080`
- `token`: 你的认证 token

### 6. 前端 Vue 示例

```vue
<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="灾害类型">
        <el-select v-model="queryParams.disasterType" placeholder="请选择灾害类型" clearable>
          <el-option label="暴雨" value="rain" />
          <el-option label="地震" value="earthquake" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 灾害列表 -->
    <el-table v-loading="loading" :data="disasterList">
      <el-table-column label="灾害 ID" prop="disasterId" />
      <el-table-column label="灾害名称" prop="disasterName" />
      <el-table-column label="灾害类型" prop="disasterType">
        <template #default="scope">
          <el-tag v-if="scope.row.disasterType === 'rain'">暴雨</el-tag>
          <el-tag v-else-if="scope.row.disasterType === 'earthquake'" type="warning">地震</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发生时间" prop="occurrenceTime" width="180" />
      <el-table-column label="位置" prop="position" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small"
            @click="handleFileList(scope.row)"
          >
            查看文件
          </el-button>
          <el-button 
            type="danger" 
            size="small"
            @click="handleDelete(scope.row)"
          >
            删除文件
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 文件列表对话框 -->
    <el-dialog title="文件列表" v-model="fileDialogVisible" width="800px">
      <el-table :data="fileList">
        <el-table-column label="文件名" prop="fileName" />
        <el-table-column label="文件路径" prop="filePath" />
        <el-table-column label="文件大小" prop="fileSize" />
        <el-table-column label="文件类型" prop="fileType" />
        <el-table-column label="上传人" prop="uploadBy" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="DisasterFile">
import { selectDisasterList, selectFilesByDisasterId, deleteFilesByDisasterId } from "@/api/disaster/file";

const { proxy } = getCurrentInstance();

const disasterList = ref([]);
const fileList = ref([]);
const loading = ref(true);
const fileDialogVisible = ref(false);

const queryParams = ref({
  disasterType: ""
});

const currentDisaster = ref({});

/** 查询灾害列表 */
function handleQuery() {
  loading.value = true;
  selectDisasterList(queryParams.value.disasterType).then(response => {
    disasterList.value = response.data;
    loading.value = false;
  });
}

/** 查看文件列表 */
function handleFileList(row) {
  currentDisaster.value = row;
  fileDialogVisible.value = true;
  selectFilesByDisasterId(row.disasterId, row.disasterType).then(response => {
    fileList.value = response.data;
  });
}

/** 删除灾害文件 */
function handleDelete(row) {
  proxy.$Modal.confirm('是否确认删除该灾害的所有文件？').then(function() {
    return deleteFilesByDisasterId(row.disasterId, row.disasterType);
  }).then(() => {
    proxy.$Modal.msgSuccess("删除成功");
    handleQuery();
  }).catch(() => {});
}

handleQuery();
</script>
```

### 7. 注意事项

1. **权限要求**：
   - 查询操作需要 `disaster:file:list` 权限
   - 删除操作需要 `disaster:file:remove` 权限

2. **删除限制**：
   - 只能按整场灾害进行删除，不能删除单个文件
   - 删除是逻辑删除，不会真正从数据库中清除数据

3. **数据一致性**：
   - 确保 disasterId 和 disasterType 的匹配性
   - 暴雨灾害的 disasterId 来自 rain_list 表的 rain_id
   - 地震灾害的 disasterId 来自 xian_earthquake_list 表的 disaster_id

4. **性能优化**：
   - 建议在 disaster_id 和 disaster_type 字段上建立索引
   - 对于大量数据，考虑分页查询
