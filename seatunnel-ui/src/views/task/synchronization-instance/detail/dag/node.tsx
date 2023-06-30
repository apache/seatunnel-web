import { defineComponent, inject } from 'vue'
import styles from './index.module.scss'

const Node = defineComponent({
  name: 'Node',
  setup() {
    const getNode = inject('getNode') as any
    const node = getNode()
    const { name } = node.getData()
    return () => (
      <div class={styles['dag-node']}>
        <span class={styles['dag-node-label']}>{name}</span>
      </div>
    )
  }
})

export default Node
