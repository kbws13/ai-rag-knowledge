package xyz.kbws.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2025/4/20
 * @description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private String code;

    private T data;

    private String info;
}
