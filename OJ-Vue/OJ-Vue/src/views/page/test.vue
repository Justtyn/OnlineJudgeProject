<script setup>
const student = {
  name: "Justyn",
  age: 20,
  major: "软件工程",
  sayHello: function () {
    console.log(`你好，我叫${this.name}，我今年${this.age}岁，专业是${this.major}。`);
  }
};
student.sayHello();
console.log(JSON.stringify(student));


</script>

<template>

</template>

<style scoped>

</style>