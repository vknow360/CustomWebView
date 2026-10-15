export const MCP_PROTOCOL_VERSION = '2024-11-05';

const SERVER_INFO = {
  name: 'appinventor-mcp-bridge',
  version: '0.1.0'
};

export const TOOL_DEFINITIONS = [
  {
    name: 'get_project_info',
    description: 'Get current project ID, name, editor state, and active screen',
    inputSchema: { type: 'object', properties: {}, required: [] }
  },
  {
    name: 'get_component_tree',
    description: 'Get the full SCM component tree for a screen',
    inputSchema: {
      type: 'object',
      properties: { screenName: { type: 'string', description: 'Screen name (default: Screen1)' } },
      required: []
    }
  },
  {
    name: 'get_component_schema',
    description: 'Get property/event/method schema for a component type',
    inputSchema: {
      type: 'object',
      properties: { componentType: { type: 'string' } },
      required: ['componentType']
    }
  },
  {
    name: 'get_all_component_types',
    description: 'Get catalog of all 107 component types with categories',
    inputSchema: { type: 'object', properties: {}, required: [] }
  },
  {
    name: 'get_blocks',
    description: 'Get blocks as XML or structured summary',
    inputSchema: {
      type: 'object',
      properties: { format: { type: 'string', enum: ['xml', 'summary'] } },
      required: []
    }
  },
  {
    name: 'get_block_diagnostics',
    description: 'Get warnings, orphaned blocks, and block counts',
    inputSchema: { type: 'object', properties: {}, required: [] }
  },
  {
    name: 'search_components',
    description: 'Search component types by keyword',
    inputSchema: {
      type: 'object',
      properties: { query: { type: 'string' } },
      required: ['query']
    }
  },
  {
    name: 'add_components',
    description: 'Add components to the screen. Default merge mode preserves existing components. Use parent param to nest inside a specific component.',
    inputSchema: {
      type: 'object',
      properties: {
        screenName: { type: 'string' },
        mode: { type: 'string', enum: ['merge', 'replace'], description: 'merge (default) preserves existing components, replace wipes all' },
        parent: { type: 'string', description: 'Parent component name to nest inside (e.g. "CustomerPanel")' },
        components: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              type: { type: 'string' },
              name: { type: 'string' },
              properties: { type: 'object' },
              children: { type: 'array' }
            },
            required: ['type', 'name']
          }
        }
      },
      required: ['components']
    }
  },
  {
    name: 'update_component_properties',
    description: 'Update properties on an existing component',
    inputSchema: {
      type: 'object',
      properties: {
        screenName: { type: 'string' },
        componentName: { type: 'string' },
        properties: { type: 'object' }
      },
      required: ['componentName', 'properties']
    }
  },
  {
    name: 'remove_components',
    description: 'Remove components from the screen',
    inputSchema: {
      type: 'object',
      properties: {
        screenName: { type: 'string' },
        componentNames: { type: 'array', items: { type: 'string' } }
      },
      required: ['componentNames']
    }
  },
  {
    name: 'add_blocks',
    description: 'Add blocks via raw XML or structured descriptions',
    inputSchema: {
      type: 'object',
      properties: {
        xml: { type: 'string' },
        blocks: { type: 'array' }
      },
      required: []
    }
  },
  {
    name: 'clear_blocks',
    description: 'Clear all blocks or specific blocks by ID',
    inputSchema: {
      type: 'object',
      properties: {
        blockIds: { type: 'array', items: { type: 'string' } }
      },
      required: []
    }
  },
  {
    name: 'modify_block',
    description: 'Modify a field value on an existing block',
    inputSchema: {
      type: 'object',
      properties: {
        blockId: { type: 'string' },
        fieldName: { type: 'string' },
        newValue: { type: 'string' }
      },
      required: ['blockId', 'fieldName', 'newValue']
    }
  },
  {
    name: 'take_screenshot',
    description: 'Capture screenshot of designer, blocks editor, or full page',
    inputSchema: {
      type: 'object',
      properties: { area: { type: 'string', enum: ['designer', 'blocks', 'full'] } },
      required: []
    }
  },
  {
    name: 'reload_designer',
    description: 'Reload the designer view',
    inputSchema: { type: 'object', properties: {}, required: [] }
  }
];

const VALID_TOOL_NAMES = new Set(TOOL_DEFINITIONS.map(t => t.name));

const RESOURCE_DEFINITIONS = [
  {
    uri: 'appinventor://component-catalog',
    name: 'Component Catalog',
    description: 'Full catalog of 107 App Inventor component types with categories and versions',
    content: 'Component Catalog: 107 component types across categories (User Interface, Layout, Media, Drawing and Animation, Maps, Sensors, Social, Storage, Connectivity, LEGO MINDSTORMS, Experimental). Use get_all_component_types tool for live data.'
  },
  {
    uri: 'appinventor://block-types',
    name: 'Block Types',
    description: 'All 201 block types organized by category',
    content: 'Block Types: component_event, component_set_get (set/get), component_method, controls_if, controls_forRange, controls_forEach, controls_while, logic_boolean, logic_compare, logic_operation, logic_negate, math_number, math_arithmetic, text, text_join, lists_create_with, lists_add_items, procedures_defnoreturn, procedures_defreturn, procedures_callnoreturn, procedures_callreturn, global_declaration, lexical_variable_get, lexical_variable_set.'
  },
  {
    uri: 'appinventor://scm-format',
    name: 'SCM Format',
    description: 'SCM JSON format specification with examples',
    content: 'SCM Format: JSON tree with root {authURL, YaVersion, Source:"Form", Properties:{$Name, $Type, $Version, Uuid, $Components:[...]}}. Properties are strings. Uuid "0" reserved for Form. Wrapper: #\\!\\n$JSON\\n{json}\\n\\!#'
  },
  {
    uri: 'appinventor://bky-format',
    name: 'BKY Format',
    description: 'BKY XML format specification with block patterns',
    content: 'BKY Format: XML with <xml xmlns="https://developers.google.com/blockly/xml"> root. Block types: component_event (mutation: component_type, instance_name, event_name), component_set_get (mutation: set_or_get, property_name, is_generic), component_method (mutation: method_name). CRITICAL: mutation before field elements.'
  },
  {
    uri: 'appinventor://current-project',
    name: 'Current Project',
    description: 'Live snapshot of current project state',
    content: 'Use get_project_info and get_component_tree tools for live project state.'
  },
  {
    uri: 'appinventor://agent-guide',
    name: 'Agent Guide',
    description: 'System prompt teaching AI agents App Inventor concepts and patterns',
    content: 'Agent Guide: 1) Read project state with get_project_info + get_component_tree. 2) Add UI with add_components (batched). 3) Add logic with add_blocks (structured or XML). 4) Verify with get_block_diagnostics. Always batch component changes into single save2. Reload required after component changes. Validate component names exist before adding blocks.'
  }
];

const RESOURCE_MAP = new Map(RESOURCE_DEFINITIONS.map(r => [r.uri, r]));

function validateRequest(request) {
  if (request.jsonrpc !== '2.0') {
    return { code: -32600, message: 'Invalid Request: missing or wrong jsonrpc version' };
  }
  if (!request.method) {
    return { code: -32600, message: 'Invalid Request: missing method' };
  }
  return null;
}

function errorResponse(id, code, message) {
  return { jsonrpc: '2.0', id: id ?? null, error: { code, message } };
}

function successResponse(id, result) {
  return { jsonrpc: '2.0', id, result };
}

export async function handleMessage(request, toolExecutor) {
  const validationError = validateRequest(request);
  if (validationError) {
    if (!('id' in request)) return null;
    return errorResponse(request.id, validationError.code, validationError.message);
  }

  const { method, params = {}, id } = request;
  const isNotification = !('id' in request);

  switch (method) {
    case 'initialize':
      if (isNotification) return null;
      return successResponse(id, {
        protocolVersion: MCP_PROTOCOL_VERSION,
        serverInfo: SERVER_INFO,
        capabilities: {
          tools: {},
          resources: {}
        }
      });

    case 'notifications/initialized':
    case 'initialized':
      return null;

    case 'tools/list':
      if (isNotification) return null;
      return successResponse(id, { tools: TOOL_DEFINITIONS });

    case 'resources/list':
      return successResponse(id, {
        resources: RESOURCE_DEFINITIONS.map(({ uri, name, description }) => ({ uri, name, description }))
      });

    case 'resources/read': {
      const { uri } = params;
      const resource = RESOURCE_MAP.get(uri);
      if (!resource) {
        return errorResponse(id, -32602, `Unknown resource: ${uri}`);
      }
      return successResponse(id, {
        contents: [{ uri, mimeType: 'text/plain', text: resource.content }]
      });
    }

    case 'tools/call': {
      const { name, arguments: args = {} } = params;

      if (!name) {
        return errorResponse(id, -32602, 'Invalid params: missing tool name');
      }

      if (!VALID_TOOL_NAMES.has(name)) {
        return errorResponse(id, -32602, `Invalid params: unknown tool "${name}"`);
      }

      const result = await toolExecutor(name, args);

      if (result.success === false) {
        return successResponse(id, {
          isError: true,
          content: [{ type: 'text', text: JSON.stringify(result) }]
        });
      }

      return successResponse(id, {
        content: [{ type: 'text', text: JSON.stringify(result) }]
      });
    }

    default:
      if (isNotification) return null;
      return errorResponse(id, -32601, `Method not found: ${method}`);
  }
}

