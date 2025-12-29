<template>
  <div>
    <h2>教师用户管理</h2>
    <p>功能描述：新增教师账户、维护教师信息、重置密码等 (对应功能 1.2)</p>
    <hr>
    <table border="1" cellpadding="10" style="width: 100%; border-collapse: collapse;">
      <thead><tr><th>教工号</th><th>姓名</th><th>职称</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="t in teachers" :key="t.id">
          <td>{{ t.id }}</td>
          <td>{{ t.name }}</td>
          <td>{{ t.title }}</td>
          <td><button>编辑</button> <button>重置密码</button></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      teachers: [
        { id: 'T001', name: '王教授', title: '教授' },
        { id: 'T002', name: '李讲师', title: '讲师' }
      ]
    }
  }
}
</script>