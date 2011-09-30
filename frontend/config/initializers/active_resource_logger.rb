if Rails.env.development?
  module ActiveResource
    class LogSubscriber < ActiveSupport::LogSubscriber
      def request(event)
        result = event.payload[:result]
        color_info :green, "#{event.payload[:method].to_s.upcase} #{event.payload[:request_uri]}"
        color_info :green, "--> %d %s %d (%.1fms)" % [result.code, result.message, result.body.to_s.length, event.duration]
        color_info :yellow, result.body.to_s
      end

      def color_info(color, msg)
        info(msg.respond_to?(color) ? msg.send(color) : msg)
      end

    end

    LogSubscriber.colorize_logging = true
  end

  ActiveResource::Base.logger = Logger.new(STDERR)
end
