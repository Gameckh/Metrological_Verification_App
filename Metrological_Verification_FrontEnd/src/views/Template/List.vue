<template>
  <div class="list">
    <el-row class="toolbar">
      <el-col :span="12">
        <el-button-group>
          <el-button type="primary" @click="designTemplate()"
            >新建模板</el-button
          >
          <el-button type="primary" @click="updateTemplate()"
            >编辑模板</el-button
          >
          <el-button type="primary" @click="lookReport()">预览模板</el-button>
        </el-button-group>
        <el-button-group>
          <el-button @click="addRecord()">填报</el-button>
          <el-button @click="goRcordList()">查看填报记录</el-button>
        </el-button-group>
      </el-col>
      <el-col :span="12">
        <el-button
          @click="mergePrint()"
          style="float: right"
          v-bind:disabled="canPrint"
          type="primary"
          >合并打印</el-button
        >
        <el-button
          @click="mergePrint()"
          style="float: right; margin-right: 5px"
          v-bind:disabled="canCustomPrint"
          type="primary"
          >自定义分页打印</el-button
        >
      </el-col>
    </el-row>

    <el-table
      ref="singleTable"
      :data="data"
      border
      highlight-current-row
      stripe
      style="width: 100%"
      @current-change="handleCurrentChange"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"> </el-table-column>
      <el-table-column prop="ID" label="ID" width="360"></el-table-column>
      <el-table-column prop="Name" label="Name" width="360"></el-table-column>
      <el-table-column prop="FileName" label="FileName"></el-table-column>
    </el-table>
  </div>
</template>

<script>
// @ is an alias to /src

import HttpUtils from "../../utils/httpUtils";

export default {
  name: "List",
  components: {},
  data: function () {
    return {
      data: [],
      currentItem: null,
      canPrint: true,
      printItems: [],
      canCustomPrint: true,
    };
  },
  created: async function () {
    let templateList = await HttpUtils.get("template/list");
    debugger;
    this.data = templateList;
    console.log(templateList);
    if (this.data.length) {
      this.$refs.singleTable.setCurrentRow(this.data[0]);
    }
  },
  methods: {
    handleCurrentChange: function (item) {
      this.currentItem = item;
    },
    mergePrint() {
      this.$router.push({
        path: "/mergePrint",
        query: {
          id: this.printItems,
          path: "template",
          schema: "template",
        },
      });
    },
    handleSelectionChange(val) {
      debugger;
      if (val.length > 1) {
        this.canPrint = false;
        for (let i = 0; i < val.length; i++) {
          if (val[i].ID != "444444444") {
            this.printItems.push(val[i].ID);
          }
        }
      } else if (val.length == 1 && val[0].ID == "444444444") {
        this.canCustomPrint = false;
        this.printItems.push(val[0].ID);
      } else {
        this.canPrint = true;
        this.canCustomPrint = true;
      }
      this.multipleSelection = val;
      console.log(val);
    },

    designTemplate() {
      this.$router.push({
        path: "/design",
        query: {
          path: "template",
          schema: "template",
        },
      });
    },
    updateTemplate() {
      this.$router.push({
        path: "/design",
        query: {
          id: this.currentItem.ID,
          path: "template",
          schema: "template",
        },
      });
    },

    addRecord() {
      this.$router.push({
        path: "/record/viewer",
        query: {
          templateId: this.currentItem.ID,
          rule: "user",
        },
      });
    },
    goRcordList() {
      this.$router.push({
        path: "/record/list",
        query: {
          templateId: this.currentItem.ID,
        },
      });
    },
    async lookReport() {
      let blob = await HttpUtils.get(
        "report/bindPDF?templateId=" + this.currentItem.ID,
        { responseType: "blob" }
      );
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    },
  },
};
</script>
<style scoped>
.list {
  text-align: left;
}
</style>