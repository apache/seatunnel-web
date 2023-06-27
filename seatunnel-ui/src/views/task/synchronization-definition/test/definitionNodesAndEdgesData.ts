export default {
  edges: [
    {
      inputPluginId: '1',
      targetPluginId: '2'
    }
  ],
  plugins: [
    {
      config: '',
      connectorType: '',
      dataSourceId: '8600164027328',
      name: 'test-1',
      pluginId: '1',
      sceneMode: 'SINGLE_TABLE',
      selectTableFields: {
        all: false,
        tableFields: []
      },
      tableOptions: {
        databases: [],
        tables: []
      },
      type: 'source'
    },
    {
      config: '',
      connectorType: '',
      dataSourceId: '8600164027328',
      name: 'test-2',
      pluginId: '2',
      sceneMode: 'SINGLE_TABLE',
      selectTableFields: {
        all: false,
        tableFields: []
      },
      tableOptions: {
        databases: [],
        tables: []
      },
      type: 'sink'
    }
  ]
}
