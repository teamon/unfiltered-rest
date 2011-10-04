require "active_record/connection_adapters/column"

module ActiveResource
  class Base
    def load(attributes, remove_root = false)
      raise ArgumentError, "expected an attributes Hash, got #{attributes.inspect}" unless attributes.is_a?(Hash)
      @prefix_options, attributes = split_options(attributes)

      if attributes.keys.size == 1
        remove_root = self.class.element_name == attributes.keys.first.to_s
      end

      attributes = Formats.remove_root(attributes) if remove_root

      attributes.each do |key, value|
        @attributes[key.to_s] =
          case value
            when Array
              resource = nil
              value.map do |attrs|
                if attrs.is_a?(Hash)
                  resource ||= find_or_create_resource_for_collection(key)
                  resource.new(attrs)
                else
                  attrs.duplicable? ? attrs.dup : attrs
                end
              end
            when Hash
              resource = find_or_create_resource_for(key)
              resource.new(value)
            else
              type_cast(key.to_s, value.duplicable? ? value.dup : value)
          end
      end
      self
    end

    def type_cast(name, value)
      if column = self.class.columns_hash[name]
        column.type_cast(value)
      else
        value
      end
    end

    class << self
      def columns_hash
        @columns_hash ||= Hash[schema.map do |key, type|
          [key, ActiveRecord::ConnectionAdapters::Column.new(key, nil, type)]
        end]
      end
    end
  end
end

class Base < ActiveResource::Base
  include ActiveResource::Validations
  include ActiveModel::Validations
  include ActiveModel::Validations::Callbacks

  self.site = "http://localhost:8080/"
end

