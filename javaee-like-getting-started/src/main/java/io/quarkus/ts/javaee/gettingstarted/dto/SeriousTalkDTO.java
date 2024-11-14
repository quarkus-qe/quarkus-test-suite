package io.quarkus.ts.javaee.gettingstarted.dto;

public class SeriousTalkDTO {
    private final DataDTO data;

    public SeriousTalkDTO(Builder builder) {
        this.data = new DataDTO(String.join(" ", builder.before, builder.start, builder.middle, builder.end,
                builder.after + builder.postscript));
    }

    public DataDTO getData() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String start;
        private String middle;
        private String end;
        private String before;
        private String after;
        private String postscript;
        private Ignored ignored = null;

        public Builder ignored(Ignored ignored) {
            throw new IllegalStateException("Property 'ignored' should be ignored");
        }

        public Builder end(SeriousTalkDTO.DataDTO endData) {
            this.end = endData.innerData();
            return this;
        }

        public Builder start(SeriousTalkDTO.DataDTO startData) {
            this.start = startData.innerData();
            return this;
        }

        public Builder middle(SeriousTalkDTO.DataDTO middleData) {
            this.middle = middleData.innerData();
            return this;
        }

        public Builder before(String before) {
            this.before = before;
            return this;
        }

        public Builder after(String after) {
            this.after = after;
            return this;
        }

        public Builder postscript(String postscript) {
            this.postscript = postscript;
            return this;
        }

        public SeriousTalkDTO build() {
            return new SeriousTalkDTO(this);
        }
    }

    public record DataDTO(String innerData) {
    }

    public interface Ignored {
    }
}
